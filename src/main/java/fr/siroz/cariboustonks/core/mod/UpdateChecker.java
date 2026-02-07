package fr.siroz.cariboustonks.core.mod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.service.json.GsonProvider;
import fr.siroz.cariboustonks.core.service.scheduler.AsyncScheduler;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.SkyBlockEvents;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.http.Http;
import fr.siroz.cariboustonks.util.http.HttpResponse;
import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.SemanticVersion;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.sounds.SoundEvents;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

final class UpdateChecker {

	private static final String MODRINTH_VERSION_URL = "https://modrinth.com/mod/cariboustonks/version/";
	private static final String MODRINTH_VERSION_CHECKER_URL = "https://api.modrinth.com/v2/project/fraWWQSJ/version?loaders=[%22fabric%22]&game_versions=";
	private static final Comparator<Version> COMPARATOR = Version::compareTo;

	private ModrinthVersionInfo newestModrinthVersionInfo = null;
	private boolean notified = false;

	UpdateChecker() {
		ClientLifecycleEvents.CLIENT_STARTED.register(_client -> this.checkUpdateOnModrinth());
		SkyBlockEvents.JOIN_EVENT.register(_s -> this.onJoinSkyBlock());
	}

	@EventHandler(event = "ClientLifecycleEvents.CLIENT_STARTED")
	private void checkUpdateOnModrinth() {
		final String mcVersion = SharedConstants.getCurrentVersion().id();
		CompletableFuture.runAsync(() -> {
			try (HttpResponse response = Http.request(MODRINTH_VERSION_CHECKER_URL + "[%22" + mcVersion + "%22]")) {
				if (!response.success()) {
					CaribouStonks.LOGGER.warn("[CaribouStonks UpdateChecker] Unable to fetch Modrinth API, code: {}", response.statusCode());
					return;
				}

				JsonArray jsonResponse = GsonProvider.prettyPrinting().fromJson(response.content(), JsonArray.class);
				if (jsonResponse == null || jsonResponse.isEmpty()) {
					CaribouStonks.LOGGER.info("[CaribouStonks UpdateChecker] No versions available on this version!");
					return;
				}

				List<ModrinthVersionInfo> modrinthVersionInfos = new ArrayList<>();
				for (JsonElement element : jsonResponse) {
					JsonObject jsonObject = element.getAsJsonObject();
					// "id": "aVm4iKzJ"
					String id = jsonObject.get("id").getAsString();
					// "name": "CaribouStonks 0.4.2+1.21.5"
					String name = jsonObject.get("name").getAsString();
					// "version_number": "0.4.2+1.21.5"
					SemanticVersion version = getVersionFromString(jsonObject.get("version_number").getAsString());
					// "changelog": "## Features\n\n- Add /stonks Command: Display...'
					//String changelog = jsonObject.has("changelog") ? jsonObject.get("changelog").getAsString() : null;

					modrinthVersionInfos.add(new ModrinthVersionInfo(id, name, version, null));
				}

				SemanticVersion currentVersion = (SemanticVersion) CaribouStonks.VERSION;
				Optional<ModrinthVersionInfo> newestModrinthVersionInfo = modrinthVersionInfos.stream()
						.filter(info -> COMPARATOR.compare(info.version(), currentVersion) > 0)
						.max(Comparator.comparing(ModrinthVersionInfo::version, COMPARATOR));

				if (newestModrinthVersionInfo.isPresent()) {
					this.newestModrinthVersionInfo = newestModrinthVersionInfo.get();

					CaribouStonks.LOGGER.info("[CaribouStonks UpdateChecker] Found a new version! o/ ({} -> {})",
							CaribouStonks.VERSION.getFriendlyString(), newestModrinthVersionInfo.get().version().getFriendlyString());
				} else {
					CaribouStonks.LOGGER.info("[CaribouStonks UpdateChecker] Up to the date!");
				}
			} catch (Exception ex) {
				CaribouStonks.LOGGER.error("[CaribouStonks UpdateChecker] Failed to check updates on Modrinth :/", ex);
			}
		}, AsyncScheduler.getInstance().blockingExecutor());
	}

	@EventHandler(event = "SkyBlockEvents.JOIN")
	private void onJoinSkyBlock() {
		if (notified || newestModrinthVersionInfo == null) return;
		notified = true;

		TickScheduler.getInstance().runLater(() -> {
			Client.sendMessage(Component.empty());
			Client.sendMessageWithPrefix(Component.empty()
					.append(Component.literal("?i").withStyle(ChatFormatting.GOLD, ChatFormatting.OBFUSCATED))
					.append(Component.literal(" Update Available! ").withStyle(ChatFormatting.YELLOW))
					.append(Component.literal("i?").withStyle(ChatFormatting.GOLD, ChatFormatting.OBFUSCATED)));
			Client.sendMessageWithPrefix(Component.empty()
					.append(Component.literal(newestModrinthVersionInfo.name()).withStyle(ChatFormatting.AQUA))
					.append(Component.literal(" CLICK").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
					.withStyle(style -> style
							.withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to open on Modrinth!").withStyle(ChatFormatting.YELLOW)))
							.withClickEvent(new ClickEvent.OpenUrl(URI.create(MODRINTH_VERSION_URL + newestModrinthVersionInfo.id())))));
			Client.sendMessage(Component.empty());

			Client.playSound(SoundEvents.TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 1f, 1f);
			Client.showNotificationSystem("CaribouStonks Update Available!", "Update to " + newestModrinthVersionInfo.version().getFriendlyString());
		}, 3, TimeUnit.SECONDS);
	}

	private @NonNull SemanticVersion getVersionFromString(@NonNull String versionNumber) throws VersionParsingException {
		if (versionNumber.charAt(0) == 'v') {
			versionNumber = versionNumber.substring(1);
		}

		return SemanticVersion.parse(versionNumber);
	}

	private record ModrinthVersionInfo(String id, String name, SemanticVersion version, @Nullable String changelog) {
	}
}
