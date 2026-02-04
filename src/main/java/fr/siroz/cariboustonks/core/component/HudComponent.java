package fr.siroz.cariboustonks.core.component;

import fr.siroz.cariboustonks.core.module.hud.Hud;
import java.util.Objects;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public final class HudComponent implements Component { // SIROZ-NOTE: documentation
	private final Identifier layerType;
	private final Identifier hudId;
	private final Hud hud;

	private HudComponent(Identifier layerType, Identifier hudId, Hud hud) {
		this.layerType = layerType;
		this.hudId = hudId;
		this.hud = hud;
	}

	public Identifier getLayerType() {
		return layerType;
	}

	public Identifier getHudId() {
		return hudId;
	}

	public Hud getHud() {
		return hud;
	}

	@NonNull
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Identifier layerType;
		private Identifier hudId;
		private Hud hud;

		public Builder attachAfter(@NonNull Identifier layerType, @NonNull Identifier hudId) {
			this.layerType = layerType;
			this.hudId = hudId;
			return this;
		}

		public Builder attachAfterStatusEffects(@NonNull Identifier hudId) {
			this.layerType = VanillaHudElements.STATUS_EFFECTS;
			this.hudId = hudId;
			return this;
		}

		public Builder hud(@NonNull Hud hud) {
			this.hud = hud;
			return this;
		}

		public HudComponent build() {
			Objects.requireNonNull(layerType, "Layer type must be set");
			Objects.requireNonNull(hudId, "Layer ID must be set");
			Objects.requireNonNull(hud, "HUD must be set");
			return new HudComponent(layerType, hudId, hud);
		}
	}
}
