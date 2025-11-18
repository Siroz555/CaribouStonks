package fr.siroz.cariboustonks.screen.stonks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

abstract class AbstractStonksWidget {

	protected final TextRenderer textRenderer;
	protected int width;
	protected int height;

	AbstractStonksWidget(int width, int height) {
		this.textRenderer = MinecraftClient.getInstance().textRenderer;
		this.width = width;
		this.height = height;
	}

	protected void updateWidgetPosition(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public abstract void render(DrawContext context, int mouseX, int mouseY, int x, int y);
}
