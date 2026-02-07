package fr.siroz.cariboustonks.core.mod.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fr.siroz.cariboustonks.config.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.NonNull;

@Environment(EnvType.CLIENT)
public final class ModMenuIntegration implements ModMenuApi {

	@Override
	public @NonNull ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ConfigManager::createConfigGUI;
	}
}
