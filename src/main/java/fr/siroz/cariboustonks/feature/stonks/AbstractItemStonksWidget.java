package fr.siroz.cariboustonks.feature.stonks;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public abstract class AbstractItemStonksWidget {

	protected final TextRenderer textRenderer;
	protected int width;
	protected int height;

	public AbstractItemStonksWidget(int width, int height) {
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
