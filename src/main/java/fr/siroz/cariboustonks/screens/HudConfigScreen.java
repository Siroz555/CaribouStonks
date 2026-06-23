package fr.siroz.cariboustonks.screens;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.module.color.Colors;
import fr.siroz.cariboustonks.core.module.hud.Hud;
import fr.siroz.cariboustonks.core.module.hud.HudAnchor;
import fr.siroz.cariboustonks.platform.rendering.gui.GuiRenderer;
import fr.siroz.cariboustonks.systems.HudSystem;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public final class HudConfigScreen extends CaribousStonksScreen {
	private static final int ANCHOR_DOT_SIZE = 5;

	private final List<Hud> hudList;
	private final @Nullable Screen parent;
	private @Nullable Hud selected = null;

	private HudConfigScreen(@Nullable Screen parent) {
		super(Component.nullToEmpty("HUD Config Screen"));
		this.hudList = CaribouStonks.systems().getSystem(HudSystem.class).getHudList();
		this.parent = parent;
	}

	public static @NonNull HudConfigScreen create(@Nullable Screen parent) {
		return new HudConfigScreen(parent);
	}

	@Override
	public void onRender(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta) {
		super.onRender(guiGraphics, mouseX, mouseY, delta);
		renderInstructions(guiGraphics);
		renderElements(guiGraphics);
	}

	private void renderInstructions(@NonNull GuiGraphicsExtractor guiGraphics) {
		int cx = width >> 1;
		int y = font.lineHeight * 16;
		int dy = font.lineHeight + 4;

		guiGraphics.centeredText(font, "LEFT-CLICK to select an HUD", cx, y, Colors.GRAY_RGB);
		y += dy;
		guiGraphics.centeredText(font, "RIGHT-CLICK to unselect an HUD", cx, y, Colors.GRAY_RGB);
		y += dy;
		guiGraphics.centeredText(font, "Mouse Wheel or +/- to scale an HUD", cx, y, Colors.GRAY_RGB);
		y += dy;
		guiGraphics.centeredText(font, "Press R to reset position & scale", cx, y, Colors.GRAY_RGB);
		y += dy;
		guiGraphics.centeredText(font, "Press TAB to cycle between HUDs", cx, y, Colors.GRAY_RGB);
	}

	private void renderElements(@NonNull GuiGraphicsExtractor guiGraphics) {
		for (Hud hud : hudList) {
			// le check config est déjà présent dans le render
			hud.renderScreen(guiGraphics);
		}

		renderSelectedOverlay(guiGraphics);
	}

	private void renderSelectedOverlay(@NonNull GuiGraphicsExtractor guiGraphics) {
		if (selected == null) return;

		int x = selected.x();
		int y = selected.y();
		int w = selected.width();
		int h = selected.height();

		int margin = 2;
		int bX = Math.max(x - margin, 0);
		int bY = Math.max(y - margin, 0);
		int bWidth = Math.min(x + w + margin, this.width) - bX;
		int bHeight = Math.min(y + h + margin, this.height) - bY;

		// Selection border
		GuiRenderer.submitBorder(guiGraphics, bX, bY, bWidth, bHeight, Colors.RED.asInt());

		// Petit carré pour montrer quel corner l'Anchor est
		HudAnchor anchor = selected.getAnchor();
		int dotX = (anchor == HudAnchor.TOP_RIGHT || anchor == HudAnchor.BOTTOM_RIGHT) ? bX + bWidth - ANCHOR_DOT_SIZE : bX;
		int dotY = (anchor == HudAnchor.BOTTOM_LEFT || anchor == HudAnchor.BOTTOM_RIGHT) ? bY + bHeight - ANCHOR_DOT_SIZE : bY;
		guiGraphics.fill(dotX, dotY, dotX + ANCHOR_DOT_SIZE, dotY + ANCHOR_DOT_SIZE, Colors.GOLD_RGB);

		Component status = Component.literal("Anchor: ").withColor(Colors.WHITE_RGB)
				.append(Component.literal(anchor.name()).withColor(Colors.GOLD_RGB))
				.append(Component.literal("  |  "))
				.append(Component.literal("Scale: ").withColor(Colors.WHITE_RGB))
				.append(Component.literal(String.format("%.2f", selected.scale())).withColor(Colors.AQUA_RGB));
		guiGraphics.centeredText(font, status, this.width >> 1, this.height - font.lineHeight - 4, Colors.WHITE_RGB);
	}

	@Override
	public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
		switch (click.button()) {
			// Select
			case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
				for (Hud hud : hudList) {
					if (!hud.isConfigEnabled()) continue;
					// overlapping behavior
					if (RenderUtils.pointIsInArea(click.x(), click.y(),
							hud.x(), hud.y(),
							hud.x() + hud.width(), hud.y() + hud.height()
					) && selected != hud) {
						selected = hud;
						return true;
					}
				}
			}
			// Unselect
			case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
				selected = null;
				return true;
			}
			default -> {
			}
		}

		return super.onMouseClicked(click, doubled);
	}

	@Override
	public boolean mouseDragged(@NonNull MouseButtonEvent click, double offsetX, double offsetY) {
		if (selected != null && click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			// setAbsolutePosition gère le clamp entre les border du screen et l'Anchor
			int targetX = (int) click.x() - (selected.width()  >> 1);
			int targetY = (int) click.y() - (selected.height() >> 1);
			selected.setAbsolutePosition(targetX, targetY);
		}

		return super.mouseDragged(click, offsetX, offsetY);
	}

	@Override
	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	public boolean keyPressed(KeyEvent input) {
		switch (input.input()) {
			// Scale up
			case GLFW.GLFW_KEY_EQUAL, GLFW.GLFW_KEY_KP_ADD -> {
				// Pour '=' il faut Maj (AZERTY/+, sinon ignore), et pour KP_ADD jamais besoin de shift
				if (selected != null && (input.input() != GLFW.GLFW_KEY_EQUAL || (input.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0)) {
					selected.setScale(selected.scale() + Hud.SCALE_STEP);
					return true;
				}
			}
			// Scale down
			case GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT -> {
				// Pour '-' il faut Maj (AZERTY/+, sinon ignore), et pour KP_SUBTRACT jamais besoin de shift
				if (selected != null && (input.input() != GLFW.GLFW_KEY_MINUS || (input.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0)) {
					selected.setScale(selected.scale() - Hud.SCALE_STEP);
					return true;
				}
			}
			// Reset position & scaling
			case GLFW.GLFW_KEY_R -> {
				if (selected != null) {
					selected.reset();
					return true;
				}
			}
			// Tab navigation
			case GLFW.GLFW_KEY_TAB -> {
				List<Hud> enabled = hudList.stream().filter(Hud::isConfigEnabled).toList();
				if (!enabled.isEmpty()) {
					if (selected == null) {
						selected = enabled.getFirst();
					} else {
						int index = enabled.indexOf(selected);
						int nextIndex = (index + 1) % enabled.size();
						selected = enabled.get(nextIndex);
					}

					return true;
				}
			}
			default -> {
			}
		}

		return super.keyPressed(input);
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		if (selected != null) {
			// Clamps entre MIN/MAX auto
			selected.setScale(selected.scale() + (float) scrollY * Hud.SCALE_STEP);
			return true;
		}
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public void close() {
		boolean changed = false;
		for (Hud hud : hudList) {
			if (hud.apply()) {
				changed = true;
			}
		}

		if (changed) {
			ConfigManager.saveConfig();
		}

		this.openScreen(parent);
	}
}
