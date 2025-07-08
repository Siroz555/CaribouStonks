package fr.siroz.cariboustonks.manager.waypoint.options;

import fr.siroz.cariboustonks.util.colors.Color;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class IconOption {

	private final Identifier icon;
	private final float width;
	private final float height;
	private final float textureWidth;
	private final float textureHeight;
	private final Vec3d renderOffset;
	private final Color color;
	private final float alpha;
	private final boolean scaleWithDistance;
	private final boolean throughBlocks;

	public IconOption() {
		this(null, 1f, 1f, 1f, 1f, new Vec3d(0, 0, 0), new Color(255, 255, 255), 1f, true, true);
	}

	public IconOption(
			@Nullable Identifier icon,
			float width,
			float height,
			float textureWidth,
			float textureHeight,
			@NotNull Vec3d renderOffset,
			@NotNull Color color,
			float alpha,
			boolean scaleWithDistance,
			boolean throughBlocks
	) {
		this.icon = icon;
		this.width = width;
		this.height = height;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.renderOffset = renderOffset;
		this.color = color;
		this.alpha = alpha;
		this.scaleWithDistance = scaleWithDistance;
		this.throughBlocks = throughBlocks;
	}

	@Contract(pure = true)
	public @NotNull Optional<Identifier> getIcon() {
		return Optional.ofNullable(icon);
	}

	public float getWidth() {
		return width;
	}

	public float getHeight() {
		return height;
	}

	public float getTextureWidth() {
		return textureWidth;
	}

	public float getTextureHeight() {
		return textureHeight;
	}

	public Vec3d getRenderOffset() {
		return renderOffset;
	}

	public Color getColor() {
		return color;
	}

	public float getAlpha() {
		return alpha;
	}

	public boolean isScaleWithDistance() {
		return scaleWithDistance;
	}

	public boolean isThroughBlocks() {
		return throughBlocks;
	}

	@Contract(value = " -> new", pure = true)
	public static @NotNull Builder builder() {
		return new Builder();
	}

	public static class Builder {

		protected Identifier icon = null;
		protected float width = 1f;
		protected float height = 1f;
		protected float textureWidth = 1f;
		protected float textureHeight = 1f;
		protected Vec3d renderOffset = new Vec3d(0, 0, 0);
		protected Color color = new Color(255, 255, 255);
		protected float alpha = 1f;
		protected boolean scaleWithDistance = true;
		protected boolean throughBlocks = true;

		public Builder withIcon(@Nullable Identifier icon) {
			this.icon = icon;
			return this;
		}

		public Builder withWidth(float width) {
			this.width = width;
			return this;
		}

		public Builder withHeight(float height) {
			this.height = height;
			return this;
		}

		public Builder withTextureWidth(float textureWidth) {
			this.textureWidth = textureWidth;
			return this;
		}

		public Builder withTextureHeight(float textureHeight) {
			this.textureHeight = textureHeight;
			return this;
		}

		public Builder withRenderOffset(@NotNull Vec3d renderOffset) {
			this.renderOffset = renderOffset;
			return this;
		}

		public Builder withColor(@NotNull Color color) {
			this.color = color;
			return this;
		}

		public Builder withAlpha(float alpha) {
			this.alpha = alpha;
			return this;
		}

		public Builder withScaleWithDistance(boolean scaleWithDistance) {
			this.scaleWithDistance = scaleWithDistance;
			return this;
		}

		public Builder withThroughBlocks(boolean throughBlocks) {
			this.throughBlocks = throughBlocks;
			return this;
		}

		public IconOption build() {
			return new IconOption(
					icon,
					width,
					height,
					textureWidth,
					textureHeight,
					renderOffset,
					color,
					alpha,
					scaleWithDistance,
					throughBlocks
			);
		}
	}
}
