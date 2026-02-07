package fr.siroz.cariboustonks.util.render.gui;

import fr.siroz.cariboustonks.CaribouStonks;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class StonksToast implements Toast {

	private static final Identifier TEXTURE = CaribouStonks.identifier("notification");

	private long toastTime = 0;
	private final List<FormattedCharSequence> message;
	private final int messageWidth;
	private final ItemStack icon;

	public StonksToast(Component text, ItemStack icon) {
		Font renderer = Minecraft.getInstance().font;
		this.message = renderer.split(text, 150);
		this.messageWidth = message.stream().mapToInt(renderer::width).max().orElse(150);
		this.icon = icon;
	}

	@Override
	public void render(GuiGraphics context, @NonNull Font renderer, long startTime) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, width(), height());

		int y = (height() - getInnerContentsHeight()) / 2;
		drawMessage(context, y);

		context.renderFakeItem(icon, 8, height() / 2 - 8);
	}

	private void drawMessage(GuiGraphics context, int y) {
		Font textRenderer = Minecraft.getInstance().font;
		for (FormattedCharSequence orderedText : message) {
			context.drawString(textRenderer, orderedText, 30, y, CommonColors.WHITE, false);
			y += textRenderer.lineHeight;
		}
	}

	@Override
	public int width() {
		return messageWidth + 30 + 6;
	}

	@Override
	public int height() {
		return Math.max(getInnerContentsHeight() + 12 + 2, 32);
	}

	private int getInnerContentsHeight() {
		return message.size() * 9;
	}

	@Override
	public @NonNull Visibility getWantedVisibility() {
		return this.toastTime > 10_000 ? Visibility.HIDE : Visibility.SHOW;
	}

	@Override
	public void update(@NonNull ToastManager manager, long time) {
		this.toastTime = time;
	}
}
