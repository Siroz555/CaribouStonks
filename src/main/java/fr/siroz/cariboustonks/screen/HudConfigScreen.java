package fr.siroz.cariboustonks.screen;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.system.hud.Hud;
import fr.siroz.cariboustonks.system.hud.HudSystem;
import fr.siroz.cariboustonks.rendering.gui.GuiRenderer;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class HudConfigScreen extends CaribousStonksScreen {

	private final List<Hud> hudList;
	@Nullable
	private final Screen parent;
	@Nullable
	private Hud selected = null;

	private HudConfigScreen(@Nullable Screen parent) {
		super(Component.nullToEmpty("HUD Config Screen"));
		this.hudList = CaribouStonks.systems().getSystem(HudSystem.class).getHudList();
		this.parent = parent;
	}

	@Contract("_ -> new")
	public static @NotNull HudConfigScreen create(@Nullable Screen parent) {
		return new HudConfigScreen(parent);
	}

	@Override
	public void onRender(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		super.onRender(guiGraphics, mouseX, mouseY, delta);
		renderInformations(guiGraphics);
		renderElements(guiGraphics);
	}

	private void renderInformations(@NotNull GuiGraphics guiGraphics) {
		int baseY = font.lineHeight * 8;
		int lineSpacing = font.lineHeight + 4;
		int y = baseY;
		guiGraphics.drawCenteredString(font,
				"LEFT-CLICK to select an HUD", width >> 1, y, Colors.LIGHT_GRAY.asInt());
		y += lineSpacing;
		guiGraphics.drawCenteredString(font,
				"RIGHT-CLICK to unselect an HUD", width >> 1, y, Colors.LIGHT_GRAY.asInt());
		y += lineSpacing;
		guiGraphics.drawCenteredString(font,
				"Press +/- to scale an HUD", width >> 1, y, Colors.LIGHT_GRAY.asInt());
		y += lineSpacing;
		guiGraphics.drawCenteredString(font,
				"Press R to reset an HUD's position & scale", width >> 1, y, Colors.LIGHT_GRAY.asInt());
		y += lineSpacing;
		guiGraphics.drawCenteredString(font,
				"Press TAB to cycle between HUDs", width >> 1, y, Colors.LIGHT_GRAY.asInt());
	}

	private void renderElements(@NotNull GuiGraphics guiGraphics) {
		for (Hud hud : hudList) {
			hud.renderScreen(guiGraphics);
		}

		if (selected != null) {
			int x = selected.x();
			int y = selected.y();
			int width = selected.width();
			int height = selected.height();

			int marginTop = 2;
			int marginLeft = 2;
			int bX = Math.max(x - marginLeft, 0);
			int bY = Math.max(y - marginTop, 0);
			int bWidth = Math.min(x + width + 2, this.width) - bX;
			int bHeight = Math.min(y + height + 2, this.height) - bY;

			GuiRenderer.drawBorder(guiGraphics, bX, bY, bWidth, bHeight, Colors.RED.asInt());
		}
	}

	@Override
	public boolean onMouseClicked(MouseButtonEvent click, boolean doubled) {
		switch (click.button()) {
			// Select
			case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
				for (Hud element : hudList) {
					// overlapping behaviour
					if (RenderUtils.pointIsInArea(click.x(), click.y(),
							element.x(), element.y(),
							element.x() + element.width(), element.y() + element.height()
					) && selected != element) {
						selected = element;
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
	public boolean mouseDragged(@NotNull MouseButtonEvent click, double offsetX, double offsetY) {
		if (selected != null && click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			selected.setX((int) Math.clamp(click.x() - (selected.width() >> 1), 0, this.width - selected.width()));
			selected.setY((int) Math.clamp(click.y() - (selected.height() >> 1), 0, this.height - selected.height()));
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
					selected.setScale(selected.scale() + 0.1f);
					return true;
				}
			}
			// Scale down
			case GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT -> {
				// Pour '-' il faut Maj (AZERTY/+, sinon ignore), et pour KP_SUBTRACT jamais besoin de shift
				if (selected != null && (input.input() != GLFW.GLFW_KEY_MINUS || (input.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0)) {
					selected.setScale(selected.scale() - 0.1f);
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
				if (!hudList.isEmpty()) {
					if (selected == null) {
						selected = hudList.getFirst();
					} else {
						int index = hudList.indexOf(selected);
						int nextIndex = (index + 1) % hudList.size();
						selected = hudList.get(nextIndex);
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

		minecraft.setScreen(parent);
	}
}
