package fr.siroz.cariboustonks.screens.stonks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

abstract class AbstractStonksWidget {

	protected final Font textRenderer;
	protected int width;
	protected int height;

	AbstractStonksWidget(int width, int height) {
		this.textRenderer = Minecraft.getInstance().font;
		this.width = width;
		this.height = height;
	}

	protected void updateWidgetPosition(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public abstract void render(GuiGraphics context, int mouseX, int mouseY, int x, int y);
}
