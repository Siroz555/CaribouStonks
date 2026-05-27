package fr.siroz.cariboustonks.features.fishing;

import fr.siroz.cariboustonks.core.component.EntityGlowComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.platform.mixin.accessors.DisplayEntityDataScaleAccessor;
import fr.siroz.cariboustonks.util.ColorUtils;
import fr.siroz.cariboustonks.util.math.MathUtils;
import java.util.function.Predicate;
import net.minecraft.world.entity.Display;
import net.minecraft.world.item.Items;

public class LotusAtollLilyPadFeature extends Feature {
	// Explosion = 8.15311 - 8.3138075 | Min = 0.6564853 | Max = 7.2235107 - 7.690316
	private static final float MIN_SCALE = 0.45f; //
	private static final float MAX_SCALE = 7.69f;
	private static final int GREEN = Colors.GREEN.asInt();
	private static final int YELLOW = Colors.YELLOW.asInt();
	private static final int RED = Colors.RED.asInt();
	private static final Predicate<Display.ItemDisplay> LILY_PAD_VALIDATOR =
			d -> d.getY() >= 65 && d.getY() <= 66 && d.getItemStack().is(Items.LILY_PAD);

	public LotusAtollLilyPadFeature() {
		this.addComponent(EntityGlowComponent.class, EntityGlowComponent.builder()
				.whenType(Display.ItemDisplay.class, display -> {
					if (LILY_PAD_VALIDATOR.test(display)) {
						final float scale = getScale(display);
						return scaleToColor(scale);
					}
					return EntityGlowComponent.EntityGlowStrategy.DEFAULT;
				})
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() == IslandType.LOTUS_ATOLL
				&& this.config().fishing.lotusAtoll.lilyPadHighlighter;
	}

	private float getScale(Display.ItemDisplay display) {
		try {
			// x/y/z sont identique
			return display.getEntityData().get(DisplayEntityDataScaleAccessor.getDataScale()).x();
		} catch (Exception _) {
			return 1;
		}
	}

	private int scaleToColor(float scale) {
		final float t = MathUtils.clamp((scale - MIN_SCALE) / (MAX_SCALE - MIN_SCALE), 0f, 1f);
		int rgb = t < 0.5f
				? ColorUtils.lerpRGB(GREEN, YELLOW, t * 2f)
				: ColorUtils.lerpRGB(YELLOW, RED, (t - 0.5f) * 2f);

		return ColorUtils.changeAlpha(rgb, 255);
	}
}
