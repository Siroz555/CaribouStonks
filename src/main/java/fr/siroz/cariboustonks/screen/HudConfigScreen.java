package fr.siroz.cariboustonks.screen;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.manager.hud.Hud;
import fr.siroz.cariboustonks.manager.hud.HudManager;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.RenderUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
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
		super(Text.of("HUD Config Screen"));
		this.hudList = CaribouStonks.managers().getManager(HudManager.class).getHudList();
		this.parent = parent;
	}

	@Contract("_ -> new")
	public static @NotNull HudConfigScreen create(@Nullable Screen parent) {
		return new HudConfigScreen(parent);
	}

	@Override
	public void onRender(DrawContext context, int mouseX, int mouseY, float delta) {
		super.onRender(context, mouseX, mouseY, delta);
		renderInformations(context);
		renderElements(context);
	}

	private void renderInformations(@NotNull DrawContext context) {
		int baseY = textRenderer.fontHeight * 8;
		int lineSpacing = textRenderer.fontHeight + 4;
		int y = baseY;
		context.drawCenteredTextWithShadow(textRenderer,
				"LEFT-CLICK to select an HUD", width >> 1, y, Colors.LIGHT_GRAY.asInt());
		y += lineSpacing;
		context.drawCenteredTextWithShadow(textRenderer,
				"RIGHT-CLICK to unselect an HUD", width >> 1, y, Colors.LIGHT_GRAY.asInt());
		y += lineSpacing;
		context.drawCenteredTextWithShadow(textRenderer,
				"Press +/- to scale an HUD", width >> 1, y, Colors.LIGHT_GRAY.asInt());
		y += lineSpacing;
		context.drawCenteredTextWithShadow(textRenderer,
				"Press R to reset an HUD's position & scale", width >> 1, y, Colors.LIGHT_GRAY.asInt());
		y += lineSpacing;
		context.drawCenteredTextWithShadow(textRenderer,
				"Press TAB to cycle between HUDs", width >> 1, y, Colors.LIGHT_GRAY.asInt());
	}

	private void renderElements(@NotNull DrawContext context) {
		for (Hud hud : hudList) {
			hud.renderScreen(context);
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

			context.drawBorder(bX, bY, bWidth, bHeight, Colors.RED.asInt());
		}
	}

	@Override
	public boolean onMouseClicked(double mouseX, double mouseY, int button) {
		switch (button) {
			// Select
			case GLFW.GLFW_MOUSE_BUTTON_LEFT -> {
				for (Hud element : hudList) {
					// overlapping behaviour
					if (RenderUtils.pointIsInArea(mouseX, mouseY,
							element.x(), element.y(),
							element.x() + element.width(), element.y() + element.height())
							&& selected != element) {
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

		return super.onMouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (selected != null && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			selected.setX((int) Math.clamp(mouseX - (selected.width() >> 1), 0, this.width - selected.width()));
			selected.setY((int) Math.clamp(mouseY - (selected.height() >> 1), 0, this.height - selected.height()));
		}

		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	@SuppressWarnings("checkstyle:CyclomaticComplexity")
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		switch (keyCode) {
			// Scale up
			case GLFW.GLFW_KEY_EQUAL, GLFW.GLFW_KEY_KP_ADD -> {
				// Pour '=' il faut Maj (AZERTY/+, sinon ignore), et pour KP_ADD jamais besoin de shift
				if (selected != null && (keyCode != GLFW.GLFW_KEY_EQUAL || (modifiers & GLFW.GLFW_MOD_SHIFT) != 0)) {
					selected.setScale(selected.scale() + 0.1f);
					return true;
				}
			}
			// Scale down
			case GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT -> {
				// Pour '-' il faut Maj (AZERTY/+, sinon ignore), et pour KP_SUBTRACT jamais besoin de shift
				if (selected != null && (keyCode != GLFW.GLFW_KEY_MINUS || (modifiers & GLFW.GLFW_MOD_SHIFT) != 0)) {
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

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void onClose() {
		boolean changed = false;
		for (Hud hud : hudList) {
			if (hud.apply()) {
				changed = true;
			}
		}

		if (changed) {
			ConfigManager.saveConfig();
		}

		assert client != null;
		client.setScreen(parent);
	}
}
