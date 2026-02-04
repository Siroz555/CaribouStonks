package fr.siroz.cariboustonks.util;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.registries.VanillaRegistries;

public final class DeveloperTools {

	private static final boolean SYSTEM_DEBUG = Boolean.parseBoolean(System.getProperty("stonks.debug", "false"));
	private static final HolderLookup.Provider LOOKUP = VanillaRegistries.createLookup();

	private DeveloperTools() {
		throw new UnsupportedOperationException();
	}

	public static boolean isInDevelopment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment()
				|| !SharedConstants.getCurrentVersion().stable()
				|| SYSTEM_DEBUG;
	}

	public static boolean isSnapshot() {
		return !SharedConstants.getCurrentVersion().stable();
	}

	public static HolderLookup.Provider getRegistryLookup() {
		Minecraft client = Minecraft.getInstance();
		return client != null && client.getConnection() != null && client.getConnection().registryAccess() != null
				? client.getConnection().registryAccess()
				: LOOKUP;
	}
}
