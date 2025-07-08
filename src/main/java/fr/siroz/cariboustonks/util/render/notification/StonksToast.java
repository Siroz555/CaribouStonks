package fr.siroz.cariboustonks.util.render.notification;

import fr.siroz.cariboustonks.CaribouStonks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.util.List;

public class StonksToast implements Toast {

	private static final Identifier TEXTURE = CaribouStonks.identifier("notification");

	private long toastTime = 0;
	private final List<OrderedText> message;
	private final int messageWidth;
	private final ItemStack icon;

	public StonksToast(Text text, ItemStack icon) {
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		this.message = renderer.wrapLines(text, 150);
		this.messageWidth = message.stream().mapToInt(renderer::getWidth).max().orElse(150);
		this.icon = icon;
	}

	@Override
	public void draw(DrawContext context, TextRenderer renderer, long startTime) {
		context.drawGuiTexture(RenderLayer::getGuiTextured, TEXTURE, 0, 0, getWidth(), getHeight());

		int y = (getHeight() - getInnerContentsHeight()) / 2;
		drawMessage(context, y);

		context.drawItemWithoutEntity(icon, 8, getHeight() / 2 - 8);
	}

	private void drawMessage(DrawContext context, int y) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		for (OrderedText orderedText : message) {
			context.drawText(textRenderer, orderedText, 30, y, Colors.WHITE, false);
			y += textRenderer.fontHeight;
		}
	}

	@Override
	public int getWidth() {
		return messageWidth + 30 + 6;
	}

	@Override
	public int getHeight() {
		return Math.max(getInnerContentsHeight() + 12 + 2, 32);
	}

	private int getInnerContentsHeight() {
		return message.size() * 9;
	}

	@Override
	public Visibility getVisibility() {
		return this.toastTime > 10_000 ? Visibility.HIDE : Visibility.SHOW;
	}

	@Override
	public void update(ToastManager manager, long time) {
		this.toastTime = time;
	}
}
