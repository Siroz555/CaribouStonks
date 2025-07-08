package fr.siroz.cariboustonks.feature;

import net.minecraft.client.MinecraftClient;

/**
 * Représente une Feature dans le Mod. Certaines fonctionnalités sont indirectes au package
 * {@link  fr.siroz.cariboustonks.feature}, peuvent être présentent dans les {@code Mixins}.
 * <p>
 * {@link #isEnabled()} Permet de savoir l'état de la configuration actuelle si la fonctionnalité est activé.
 * L'implémentation est {@code ConfigManager.getConfig().vanilla.hideArmorOverlay} par exemple.
 * Toutefois, certaines fonctionnalités ont plusieurs options, il faudra les récupérer individuellement.
 */
public abstract class Feature {

	/**
	 * Permet de savoir si la fonctionnalité est activé ou désactivé à partir de sa configuration, de facon "globale"
	 *
	 * @return {@code true} / {@code false}
	 */
	public abstract boolean isEnabled();

	public String getShortName() {
		return this.getClass().getSimpleName().replace("Feature", "");
	}

	protected boolean checkPlayerIsNull() {
		return MinecraftClient.getInstance().player == null;
	}

	protected boolean checkWorldIsNull() {
		return MinecraftClient.getInstance().world == null;
	}

	protected boolean checkPlayerAndWorldIsNull() {
		return checkPlayerIsNull() || checkWorldIsNull();
	}
}
