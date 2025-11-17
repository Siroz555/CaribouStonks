package fr.siroz.cariboustonks.core.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fr.siroz.cariboustonks.config.ConfigManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public final class ModMenuIntegration implements ModMenuApi {

	@Contract(pure = true)
	@Override
	public @NotNull ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ConfigManager::createConfigGUI;
	}
}
