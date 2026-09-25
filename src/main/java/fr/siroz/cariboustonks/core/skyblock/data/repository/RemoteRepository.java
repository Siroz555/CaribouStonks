package fr.siroz.cariboustonks.core.skyblock.data.repository;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.module.http.Http;
import fr.siroz.cariboustonks.core.module.http.HttpResponse;
import fr.siroz.cariboustonks.core.infrastructure.json.GsonProvider;
import fr.siroz.cariboustonks.util.JsonUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class RemoteRepository {
	private static final String REPO_URL = "https://raw.githubusercontent.com/Siroz555/Caribou-REPO/main/";
	private static final String METADATA_URL = REPO_URL + "metadata.json";
	private static final int MAX_CONCURRENT_DOWNLOADS = 2; // 5

	private static final Duration FIRST_RETRY_DELAY = Duration.ofSeconds(30); // Metadata
	private static final int MAX_RETRY_ATTEMPTS = 3; // Metadata - 4 tentatives totales (1 + 3 retries)
	private static final int MAX_GLOBAL_RETRIES = 2; // Global - 3 tentatives totales (1 + 2 retries)
	private static final long[] GLOBAL_RETRY_DELAYS_SECONDS = {60L, 300L}; // Global - 1 min puis 5 min

	public static final Path DATA_DIRECTORY = CaribouStonks.CONFIG_DIR.resolve("repo/data").normalize();
	public static final Path CACHE_DIRECTORY = CaribouStonks.CONFIG_DIR.resolve("repo/cache").normalize();
	public static final Path METADATA_PATH = DATA_DIRECTORY.resolve("metadata.json");

	private final AtomicBoolean fetchInProgress = new AtomicBoolean(false);
	private final AtomicInteger retryAttempts = new AtomicInteger(0);
	private final AtomicBoolean lastFetchSuccessful = new AtomicBoolean(false);
	private final AtomicInteger globalRetryAttempts = new AtomicInteger(0);

	private final CompletableFuture<Boolean> readyFuture = new CompletableFuture<>();

	private final ExecutorService executor;
	private final boolean devMode;

	public RemoteRepository(ExecutorService executor, boolean devMode) {
		this.executor = executor;
		this.devMode = devMode;
		try {
			Files.createDirectories(DATA_DIRECTORY);
			Files.createDirectories(CACHE_DIRECTORY);
		} catch (IOException ex) {
			CaribouStonks.LOGGER.error("[RemoteRepository] Failed to create data directories", ex);
		}
		this.initialize(true);
	}

	/**
	 * Future "startup gate" qui se complète une seule fois à la fin de l'init initiale.
	 * {@code true}: données prêtes, tous fichiers vérifiés.
	 * {@code false}: tous les retries épuisés, données potentiellement incomplètes
	 */
	public @NonNull CompletableFuture<Boolean> getReadyFuture() {
		return readyFuture;
	}

	/**
	 * @return {@code true} si les données sont prêtes et aucun fetch n'est en cours
	 */
	public boolean isReady() {
		return !fetchInProgress.get() && lastFetchSuccessful.get();
	}

	/**
	 * @return {@code true} si un fetch est actuellement en cours
	 */
	public boolean isFetchInProgress() {
		return fetchInProgress.get();
	}

	/**
	 * Déclenche un reload. Pas d'auto-retry sur échec, ne re-complète pas {@link #getReadyFuture()}
	 *
	 * @return future complete state
	 */
	public @NonNull CompletableFuture<Void> reload() {
		if (fetchInProgress.get()) {
			if (devMode) CaribouStonks.LOGGER.warn("[RemoteRepository] Reload requested but fetch already in progress");
			return CompletableFuture.completedFuture(null);
		}
		CaribouStonks.LOGGER.info("[RemoteRepository] Manual reload triggered");
		globalRetryAttempts.set(0);
		lastFetchSuccessful.set(false);
		return initialize(false);
	}

	public @NonNull String getLocalVersion() {
		if (Files.notExists(METADATA_PATH)) return "0.0.0";
		try (BufferedReader reader = Files.newBufferedReader(METADATA_PATH)) {
			JsonObject json = GsonProvider.prettyPrinting().fromJson(reader, JsonObject.class);
			return json != null ? JsonUtils.getStringOrDefault(json, "version", "0.0.0") : "0.0.0";
		} catch (IOException | JsonParseException _) {
			return "0.0.0";
		}
	}

	public @NonNull Path resolveDataFile(@NonNull String relativePath) {
		Path resolved = DATA_DIRECTORY.resolve(relativePath).normalize();
		if (!resolved.startsWith(DATA_DIRECTORY)) {
			throw new SecurityException("Path traversal detected: " + relativePath);
		}
		return resolved;
	}

	private @NonNull CompletableFuture<Void> initialize(boolean autoRetry) {
		if (!fetchInProgress.compareAndSet(false, true)) {
			CaribouStonks.LOGGER.warn("[RemoteRepository] Fetch already in progress, skipping");
			return CompletableFuture.completedFuture(null);
		}
		retryAttempts.set(0);

		return loadLocalMetadata()
				.thenCompose(local -> fetchRemoteMetadata()
						.thenApply(remote -> new MetadataPair(local, remote)))
				.thenCompose(this::compareAndDownload)
				.thenRun(() -> {
					CaribouStonks.LOGGER.info("[RemoteRepository] Repository initialized successfully");
					fetchInProgress.set(false);
					lastFetchSuccessful.set(true);
					globalRetryAttempts.set(0);
					if (!readyFuture.isDone()) readyFuture.complete(true);
				})
				.exceptionally(throwable -> {
					Throwable cause = throwable instanceof CompletionException ce ? ce.getCause() : throwable;
					CaribouStonks.LOGGER.error("[RemoteRepository] Failed to initialize repository", cause);
					fetchInProgress.set(false);
					lastFetchSuccessful.set(false);
					if (autoRetry) scheduleGlobalRetry();
					return null;
				});
	}

	private void scheduleGlobalRetry() {
		int attempts = globalRetryAttempts.getAndIncrement();

		if (attempts >= MAX_GLOBAL_RETRIES) {
			CaribouStonks.LOGGER.error("[RemoteRepository] All global retries exhausted, giving up");
			if (!readyFuture.isDone()) readyFuture.complete(false);
			return;
		}

		long delaySeconds = GLOBAL_RETRY_DELAYS_SECONDS[attempts];
		CaribouStonks.LOGGER.warn("[RemoteRepository] Scheduling global auto-retry {}/{} in {}s",
				attempts + 1, MAX_GLOBAL_RETRIES, delaySeconds);

		CompletableFuture.delayedExecutor(delaySeconds, TimeUnit.SECONDS, executor)
				.execute(() -> initialize(true));
	}

	private @NonNull CompletableFuture<JsonObject> loadLocalMetadata() {
		if (Files.notExists(METADATA_PATH)) return CompletableFuture.completedFuture(new JsonObject());

		return CompletableFuture.supplyAsync(() -> {
			try (BufferedReader reader = Files.newBufferedReader(METADATA_PATH)) {
				return Objects.requireNonNullElseGet(
						GsonProvider.prettyPrinting().fromJson(reader, JsonObject.class),
						JsonObject::new
				);
			} catch (JsonParseException | IOException ex) {
				CaribouStonks.LOGGER.error("[RemoteRepository] Failed to load local metadata", ex);
				return new JsonObject();
			}
		}, executor);
	}

	private @NonNull CompletableFuture<JsonObject> fetchRemoteMetadata() {
		return CompletableFuture.supplyAsync(() -> {
			try (HttpResponse response = Http.request(METADATA_URL)) {
				if (!response.success()) {
					throw new RuntimeException("Remote metadata returned status code: " + response.statusCode());
				}
				String body = response.content();
				if (body == null || body.isBlank()) {
					throw new RuntimeException("Remote metadata returned null or blank reply");
				}
				return GsonProvider.prettyPrinting().fromJson(body, JsonObject.class);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to fetch remote metadata", ex);
			}
		}, executor).orTimeout(30, TimeUnit.SECONDS).exceptionallyCompose(throwable -> {
			Throwable cause = throwable instanceof CompletionException ce ? ce.getCause() : throwable;
			int attemptsDone = retryAttempts.getAndIncrement();
			int totalAttempts = MAX_RETRY_ATTEMPTS + 1;

			CaribouStonks.LOGGER.error("[RemoteRepository] Metadata fetch failed (attempt {}/{}). Cause: {}",
					attemptsDone + 1, totalAttempts, cause.getMessage());

			if (attemptsDone >= MAX_RETRY_ATTEMPTS) {
				CaribouStonks.LOGGER.error("[RemoteRepository] Max metadata retries reached, aborting");
				return CompletableFuture.completedFuture(null);
			}

			long delaySeconds = FIRST_RETRY_DELAY.toSeconds() << attemptsDone;
			CaribouStonks.LOGGER.warn("[RemoteRepository] Retrying metadata fetch in {}s (attempt {}/{})",
					delaySeconds, attemptsDone + 2, totalAttempts);

			Executor delayed = CompletableFuture.delayedExecutor(
					delaySeconds, TimeUnit.SECONDS, executor);
			return CompletableFuture.supplyAsync(() -> null, delayed)
					.thenCompose(_ -> fetchRemoteMetadata());
		});
	}

	private @NonNull CompletableFuture<Void> saveLocalMetadata(@NonNull JsonObject metadata) {
		return CompletableFuture.runAsync(() -> {
			try (BufferedWriter writer = Files.newBufferedWriter(METADATA_PATH)) {
				GsonProvider.prettyPrinting().toJson(metadata, writer);
			} catch (Exception ex) {
				throw new RuntimeException("Failed to save local metadata", ex);
			}
		}, executor);
	}

	private @NonNull CompletableFuture<Void> compareAndDownload(@NonNull MetadataPair pair) {
		JsonObject remoteMetadata = pair.remote();
		if (remoteMetadata == null) {
			if (devMode) CaribouStonks.LOGGER.error("[RemoteRepository] Remote metadata is null, aborting update");
			return CompletableFuture.failedFuture(new IllegalStateException("Null remote metadata"));
		}

		String remoteVersion = JsonUtils.getStringOrDefault(remoteMetadata, "version", "?");
		String localVersion = JsonUtils.getStringOrDefault(pair.local(), "version", "0.0.0");
		if (devMode) CaribouStonks.LOGGER.info("[RemoteRepository] Version: local={} remote={}", localVersion, remoteVersion);

		// Toujours vérifier les hash réels, pas de early-return sur la version seule
		return determineFilesToDownloadAsync(remoteMetadata)
				.thenCompose(files -> {
					if (files.isEmpty()) {
						CaribouStonks.LOGGER.info("[RemoteRepository] All files verified, up to date ({})", remoteVersion);
						// La version peut avoir changé sans changement de contenu : sync quand même
						if (!remoteVersion.equals(localVersion)) {
							return saveLocalMetadata(remoteMetadata);
						}
						return CompletableFuture.completedFuture(null);
					}

					CaribouStonks.LOGGER.info("[RemoteRepository] {} file(s) need updating", files.size());
					return downloadFilesAsync(files)
							.thenCompose(result -> {
								if (result.failureCount() > 0) {
									CaribouStonks.LOGGER.warn("[RemoteRepository] {} file(s) failed to download, skipping metadata save",
											result.failureCount());
									return CompletableFuture.failedFuture(new RuntimeException("Partial download failure"));
								}
								return saveLocalMetadata(remoteMetadata);
							})
							.thenRun(() -> CaribouStonks.LOGGER.info("[RemoteRepository] Repository updated to version {}", remoteVersion));
				});
	}

	/**
	 * Détermine les fichiers à télécharger en comparant les hash SHA-256 réels sur disque avec ceux du metadata distant.
	 * <p>
	 * Toujours calcule le hash réel du fichier — jamais basé uniquement sur le metadata local.
	 * Garantit la détection de toute corruption sur disque.
	 */
	private @NonNull CompletableFuture<List<FileToDownload>> determineFilesToDownloadAsync(@NonNull JsonObject remoteMetadata) {
		return CompletableFuture.supplyAsync(() -> {
			if (!remoteMetadata.has("files")) return Collections.emptyList();

			JsonObject remoteFiles = remoteMetadata.getAsJsonObject("files");
			List<FileToDownload> filesToDownload = new ArrayList<>();

			for (Map.Entry<String, JsonElement> entry : remoteFiles.entrySet()) {
				String remotePath = entry.getKey();
				JsonObject remoteFileInfo = entry.getValue().getAsJsonObject();
				String remoteHash = remoteFileInfo.has("hash")
						? remoteFileInfo.get("hash").getAsString()
						: null;

				String localRelativePath = stripDataPrefix(remotePath);
				Path localFile = DATA_DIRECTORY.resolve(localRelativePath).normalize();

				if (!localFile.startsWith(DATA_DIRECTORY)) {
					CaribouStonks.LOGGER.error("[RemoteRepository] Path traversal detected, skipping: {}", remotePath);
					continue;
				}

				boolean needsDownload = !Files.exists(localFile);
				if (!needsDownload && remoteHash != null) {
					// Check toujours le hash réel sur disque pour détecter toute corruption
					try {
						needsDownload = !remoteHash.equals(StonksUtils.calculateSHA256(localFile));
					} catch (IOException e) {
						needsDownload = true;
					}
				}

				if (needsDownload) {
					filesToDownload.add(new FileToDownload(remotePath, localRelativePath, remoteHash));
				}
			}

			CaribouStonks.LOGGER.info("[RemoteRepository] Files to download: {}", filesToDownload.size());
			return filesToDownload;
		}, executor);
	}

	private @NonNull CompletableFuture<DownloadResult> downloadFilesAsync(@NonNull List<FileToDownload> files) {
		CaribouStonks.LOGGER.info("[RemoteRepository] Starting parallel downloads ({} files)...", files.size());

		List<List<FileToDownload>> batches = StonksUtils.partitionList(files, MAX_CONCURRENT_DOWNLOADS);
		CompletableFuture<DownloadResult> batchChain = CompletableFuture.completedFuture(new DownloadResult(0, 0));

		for (int i = 0; i < batches.size(); i++) {
			final int batchIndex = i;
			final List<FileToDownload> batch = batches.get(i);

			batchChain = batchChain.thenCompose(previousResult -> {
				if (devMode) CaribouStonks.LOGGER.info("[RemoteRepository] Processing batch {}/{}", batchIndex + 1, batches.size());

				List<CompletableFuture<Boolean>> downloads = batch.stream()
						.map(this::downloadFileAsync)
						.toList();

				return CompletableFuture.allOf(downloads.toArray(new CompletableFuture[0]))
						.thenApply(_ -> {
							int successCount = (int) downloads.stream()
									.map(CompletableFuture::join)
									.filter(success -> success)
									.count();
							return new DownloadResult(
									previousResult.successCount() + successCount,
									previousResult.failureCount() + (batch.size() - successCount)
							);
						});
			});
		}

		return batchChain;
	}

	private @NonNull CompletableFuture<Boolean> downloadFileAsync(@NonNull FileToDownload file) {
		return CompletableFuture.supplyAsync(() -> {
			if (devMode) CaribouStonks.LOGGER.info("[RemoteRepository] Downloading: {}", file.remotePath());

			String url = REPO_URL + file.remotePath();
			Path targetPath = DATA_DIRECTORY.resolve(file.localPath()).normalize();
			Path tempPath = CACHE_DIRECTORY.resolve(file.localPath() + ".tmp").normalize();

			if (!targetPath.startsWith(DATA_DIRECTORY) || !tempPath.startsWith(CACHE_DIRECTORY)) {
				throw new SecurityException("Path traversal detected for: " + file.remotePath());
			}

			try {
				Files.createDirectories(tempPath.getParent());
				Files.createDirectories(targetPath.getParent());

				try (HttpResponse response = Http.request(url)) {
					if (!response.success()) {
						throw new RuntimeException("Fetch failed. Status code: " + response.statusCode());
					}
					String body = response.content();
					if (body == null || body.isBlank()) {
						throw new RuntimeException("Unable to download file; returned null or blank reply");
					}
					Files.writeString(tempPath, body, StandardCharsets.UTF_8);
				}

				if (file.expectedHash() != null) {
					String actualHash = StonksUtils.calculateSHA256(tempPath);
					if (!file.expectedHash().equals(actualHash)) {
						Files.deleteIfExists(tempPath);
						throw new RuntimeException("Hash mismatch for " + file.remotePath());
					}
				}

				Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
				CaribouStonks.LOGGER.info("[RemoteRepository] OK {}", file.remotePath());
				return true;
			} catch (Exception ex) {
				try {
					Files.deleteIfExists(tempPath);
				} catch (IOException ignored) {
				}
				throw new RuntimeException("Failed to download " + file.remotePath(), ex);
			}
		}, executor).orTimeout(30, TimeUnit.SECONDS).exceptionally(throwable -> {
			CaribouStonks.LOGGER.error("[RemoteRepository] {} download failed.", file.remotePath(), throwable);
			return false;
		});
	}

	private @NonNull String stripDataPrefix(@NonNull String remotePath) {
		// "data/attributes.json" → "attributes.json"
		return remotePath.startsWith("data/") ? remotePath.substring(5) : remotePath;
	}

	private record MetadataPair(@NonNull JsonObject local, @Nullable JsonObject remote) {
	}

	private record FileToDownload(@NonNull String remotePath, @NonNull String localPath, @Nullable String expectedHash) {
	}

	private record DownloadResult(int successCount, int failureCount) {
	}
}
