package fr.siroz.cariboustonks.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryWrapper;

public final class DeveloperTools {

	private static final boolean SYSTEM_DEBUG = Boolean.parseBoolean(System.getProperty("stonks.debug", "false"));
	private static final RegistryWrapper.WrapperLookup LOOKUP = BuiltinRegistries.createWrapperLookup();

	private DeveloperTools() {
		throw new UnsupportedOperationException();
	}

	public static boolean isInDevelopment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment()
				|| !SharedConstants.getGameVersion().stable()
				|| SYSTEM_DEBUG;
	}

	public static boolean isSnapshot() {
		return !SharedConstants.getGameVersion().stable();
	}

	public static RegistryWrapper.WrapperLookup getRegistryLookup() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client != null && client.getNetworkHandler() != null && client.getNetworkHandler().getRegistryManager() != null
				? client.getNetworkHandler().getRegistryManager()
				: LOOKUP;
	}
}
