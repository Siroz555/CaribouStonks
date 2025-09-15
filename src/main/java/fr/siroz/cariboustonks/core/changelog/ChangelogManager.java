package fr.siroz.cariboustonks.core.changelog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.json.GsonProvider;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.screen.changelog.ChangelogScreen;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.http.Http;
import fr.siroz.cariboustonks.util.http.HttpResponse;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.http.client.HttpResponseException;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Retrieves and manage the changelogs from releases (if available) from GitHub.
 */
@ApiStatus.Internal
public final class ChangelogManager {

	private static final String GITHUB_REPO_URL = "https://api.github.com/repos/Siroz555/CaribouStonks/releases";
	// Retirer les éléments après le "+" : 6.1.0+1.21.8 -> 6.1.0
	private static final String CURRENT_VERSION = CaribouStonks.VERSION.getFriendlyString().replaceAll("\\+.*$", "");
	private static final Comparator<Version> COMPARATOR = Version::compareTo;
	private static final Path LAST_SEEN_VERSION_PATH = CaribouStonks.CONFIG_DIR.resolve("last_seen_version.txt");

	private final List<ChangelogEntry> changelogEntries;
	@Nullable
	private String lastSeenVersion;
	private boolean notified;

	public ChangelogManager() {
		this.changelogEntries = new ArrayList<>();
		this.lastSeenVersion = null;
		this.notified = false;

		ClientLifecycleEvents.CLIENT_STARTED.register(this::onClientStarted);
		SkyBlockEvents.JOIN.register(_s -> this.onJoinSkyBlock());

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _ra) ->
				dispatcher.register(ClientCommandManager.literal("stonksviewchangelog")
						.executes(StonksUtils.openScreen(
								() -> ChangelogScreen.create(changelogEntries, this::markChangelogAsSeen)))));
	}

	@EventHandler(event = "ClientLifecycleEvents.CLIENT_STARTED")
	private void onClientStarted(MinecraftClient client) {
		lastSeenVersion = loadLastSeenVersion();
		// La dernière version vu depuis le fichier :
		//  Si elle est présente et que la version actuelle du mod est different > fetch changelog depuis GitHub
		//  Sinon > marquer comme "seen" (cas de la première installation depuis la version avec le changelog).
		// > Vérification avec un "equals" simple pour détecter tout changement, je ne fais pas de "*alpha" ou "*beta".
		// > Si c'est la première installation, aucun changelog ne sera fetch depuis GitHub.
		if (lastSeenVersion != null && !lastSeenVersion.equals(CURRENT_VERSION)) {
			CaribouStonks.LOGGER.info("[ChangelogManager] Last seen version is not the current version, fetching changelogs..");
			fetchChangelogSinceLastSeen(lastSeenVersion);
		} else if (lastSeenVersion == null) {
			CaribouStonks.LOGGER.info("[ChangelogManager] No changelog found, first time? Marking as seen..");
			markChangelogAsSeen();
		}
	}

	@EventHandler(event = "SkyBlockEvents.JOIN")
	private void onJoinSkyBlock() {
		if (notified || lastSeenVersion == null || changelogEntries.isEmpty()) return;
		notified = true;
		TickScheduler.getInstance().runLater(() -> {
			Client.sendMessage(Text.empty());
			Client.sendMessageWithPrefix(Text.empty()
					.append(Text.literal("Updated!").formatted(Formatting.GREEN))
					.append(Text.literal(" " + lastSeenVersion + " ").formatted(Formatting.YELLOW))
					.append(Text.literal("->").formatted(Formatting.GRAY))
					.append(Text.literal(" " + CURRENT_VERSION + " ").formatted(Formatting.GREEN)));
			Client.sendMessageWithPrefix(Text.empty()
					.append(Text.literal("Click").formatted(Formatting.YELLOW))
					.append(Text.literal(" HERE ").formatted(Formatting.YELLOW, Formatting.BOLD))
					.append(Text.literal("to see the changelogs in-game!").formatted(Formatting.YELLOW))
					.styled(style -> style
							.withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to see the changelogs!").formatted(Formatting.YELLOW)))
							.withClickEvent(new ClickEvent.RunCommand("/stonksviewchangelog"))));
			Client.sendMessage(Text.empty());
		}, 3, TimeUnit.SECONDS);
	}

	private void markChangelogAsSeen() {
		lastSeenVersion = CURRENT_VERSION;
		changelogEntries.clear();
		saveLastSeenVersion();
	}

	private void saveLastSeenVersion() {
		if (lastSeenVersion != null) {
			try (BufferedWriter writer = Files.newBufferedWriter(LAST_SEEN_VERSION_PATH)) {
				writer.write(lastSeenVersion);
			} catch (Exception ex) {
				CaribouStonks.LOGGER.error("[ChangelogManager] Unable to save last seen version to file: {}", LAST_SEEN_VERSION_PATH, ex);
			}
		}
	}

	private @Nullable String loadLastSeenVersion() {
		if (!Files.exists(LAST_SEEN_VERSION_PATH)) return null;

		try (BufferedReader reader = Files.newBufferedReader(LAST_SEEN_VERSION_PATH)) {
			String line = reader.readLine();
			if (line != null && !line.isBlank()) {
				return parse(line.trim());
			}
		} catch (Exception ex) {
			CaribouStonks.LOGGER.error("[ChangelogManager] Unable to load last seen version from file: {}", LAST_SEEN_VERSION_PATH, ex);
		}
		return null;
	}

	private @Nullable String parse(String version) {
		try {
			SemanticVersion.parse(version);
			return version;
		} catch (VersionParsingException ex) {
			CaribouStonks.LOGGER.warn("[ChangelogManager] Unable to parse version from file: {}", version, ex);
			return null;
		}
	}

	private void fetchChangelogSinceLastSeen(@NotNull String lastSeenVersionToParse) {
		CompletableFuture.runAsync(() -> {
			try (HttpResponse response = Http.request(GITHUB_REPO_URL)) {
				if (!response.success()) {
					throw new HttpResponseException(response.statusCode(), response.content());
				}

				String responseBody = response.content();
				if (responseBody == null || responseBody.isEmpty()) {
					throw new IllegalStateException("GitHub API returned an empty response body");
				}

				JsonArray releases = GsonProvider.prettyPrinting().fromJson(responseBody, JsonArray.class);
				if (releases == null || releases.isEmpty()) {
					return;
				}

				parseChangelogs(releases, lastSeenVersionToParse);
			} catch (Exception ex) {
				CaribouStonks.LOGGER.error("[ChangelogManager] Unable to fetch changelogs from GitHub", ex);
			}
		});
	}

	private void parseChangelogs(@NotNull JsonArray releases, String lastSeenVersionToParse) {
		Map<String, ChangelogEntry> changelogCache = new TreeMap<>();
		try {
			for (JsonElement element : releases) {
				JsonObject release = element.getAsJsonObject();
				String tagName = release.get("tag_name").getAsString();
				String body = release.has("body") ? release.get("body").getAsString() : "";
				String publishedAt = release.get("published_at").getAsString();
				// Parser le body pour extraire les changements
				ChangelogEntry entry = parseChangelogBody(tagName, body, publishedAt);
				changelogCache.put(tagName.replace("v", ""), entry);
			}
		} catch (Exception ex) {
			CaribouStonks.LOGGER.error("[ChangelogManager] Unable to parse changelogs from GitHub", ex);
		}

		List<ChangelogEntry> relevantChangelogs = new ArrayList<>();
		for (Map.Entry<String, ChangelogEntry> entry : changelogCache.entrySet()) {
			String gitHubVersion = entry.getKey();
			try {
				SemanticVersion currentVersionSem = SemanticVersion.parse(CURRENT_VERSION);
				SemanticVersion lastSeenVersionSem = SemanticVersion.parse(lastSeenVersionToParse);
				SemanticVersion changelogVersionSem = SemanticVersion.parse(gitHubVersion);
				// Toutes les versions entre lastSeenVersion (exclu) et currentVersion (inclus).
				if (COMPARATOR.compare(changelogVersionSem, lastSeenVersionSem) > 0 &&
						COMPARATOR.compare(changelogVersionSem, currentVersionSem) <= 0
				) {
					relevantChangelogs.add(entry.getValue());
				}
			} catch (VersionParsingException ex) {
				CaribouStonks.LOGGER.warn("[ChangelogManager] Unable to parse versions from GitHub version {}", gitHubVersion, ex);
			}
		}

		changelogEntries.addAll(relevantChangelogs);
		// Trier pour avoir la version la plus récente en premier
		changelogEntries.sort((o1, o2) -> o2.version.compareTo(o1.version));

		if (!changelogEntries.isEmpty()) {
			CaribouStonks.LOGGER.info("[ChangelogManager] Found {} relevant changelogs", changelogEntries.size());
		}

		changelogCache.clear();
		relevantChangelogs.clear();
	}

	private @NotNull ChangelogEntry parseChangelogBody(@NotNull String version, String body, @NotNull String date) {
		ChangelogEntry entry = new ChangelogEntry();
		entry.version = version.replace("v", "");
		entry.date = date.substring(0, 10); // Format: YYYY-MM-DD

		if (body == null || body.isEmpty()) {
			return entry;
		}

		String[] lines = body.split("\n");
		List<String> currentSection = null;

		for (String line : lines) {
			line = line.trim();

			if (line.toLowerCase(Locale.ENGLISH).startsWith("## features")) {
				currentSection = entry.feature;
			} else if (line.toLowerCase(Locale.ENGLISH).startsWith("## improvements")) {
				currentSection = entry.improvement;
			} else if (line.toLowerCase(Locale.ENGLISH).startsWith("## fixes")) {
				currentSection = entry.fixed;
			} else if (line.toLowerCase(Locale.ENGLISH).startsWith("## backend")) {
				currentSection = entry.backend;
			} else if (line.startsWith("-") || line.startsWith("*")) {
				// C'est un élément de liste
				if (currentSection != null) {
					String item = line.substring(1).trim();
					if (!item.isEmpty()) {
						currentSection.add(item);
					}
				}
			}
		}

		// Si aucune section n'a été trouvée, c'est dans "improvement", mais à voir, car je n'aime pas trop.
		if (entry.isEmpty()) {
			entry.improvement.add(body);
		}

		return entry;
	}
}
