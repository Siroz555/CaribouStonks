package fr.siroz.cariboustonks.util;

import fr.siroz.cariboustonks.CaribouStonks;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.GameVersion;
import net.minecraft.SharedConstants;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class DeveloperTools {

	private static final boolean SYSTEM_DEBUG = Boolean.parseBoolean(System.getProperty("stonks.debug", "false"));

	private DeveloperTools() {
	}

	public static void initDeveloperTools() {
		if (isInDevelopment()) {
			GameVersion version = SharedConstants.getGameVersion();
			CaribouStonks.LOGGER.warn("Debug mode enabled ({}) {}", version.getName(), DeveloperTools.isSnapshot() ? "(Snapshot)" : "");

			ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
				if (client.player != null && DeveloperTools.isInDevelopment()) {
					Text snapShot = DeveloperTools.isSnapshot()
							? Text.literal(" (Snapshot)").formatted(Formatting.DARK_RED) : Text.empty();
					client.player.sendMessage(CaribouStonks.prefix().get()
							.append(Text.literal("Debug mode enabled (" + version.getName() + ")").formatted(Formatting.RED)
									.append(snapShot)), false);
				}
			});
		}
	}

	public static boolean isInDevelopment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment()
				|| !SharedConstants.getGameVersion().isStable()
				|| SYSTEM_DEBUG;
	}

	public static boolean isSnapshot() {
		return !SharedConstants.getGameVersion().isStable();
	}
}
