package fr.siroz.cariboustonks.core;

import fr.siroz.cariboustonks.CaribouStonks;
import net.fabricmc.loader.api.Version;
import net.minecraft.SharedConstants;

@Deprecated
public final class UpdateChecker {

    private static final String MODRINTH_URL = "https://api.modrinth.com/v2/project/XXX/";
    private static final Version MOD_VERSION = CaribouStonks.MOD_CONTAINER.getMetadata().getVersion();
    private static final String MC_VERSION = SharedConstants.getGameVersion().getId();

    public UpdateChecker() {

    }

    public record ModVersionInfo(String versionNumber, String changelog, String downloadUrl, String id) {
    }
}
