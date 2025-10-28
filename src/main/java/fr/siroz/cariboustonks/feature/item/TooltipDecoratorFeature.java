package fr.siroz.cariboustonks.feature.item;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.core.data.hypixel.item.Rarity;
import fr.siroz.cariboustonks.event.ItemRenderEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.rendering.gui.GuiRenderer;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.ItemStack;

import java.awt.Color;
import java.util.List;
import java.util.Map;

public class TooltipDecoratorFeature extends Feature {

	private static final Map<Rarity, Pair<Integer, Integer>> RARITY_COLORS = Map.ofEntries(
			Map.entry(Rarity.COMMON, Pair.of(new Color(252, 252, 252).getRGB(), new Color(134, 134, 134).getRGB())),
			Map.entry(Rarity.UNCOMMON, Pair.of(new Color(21, 215, 0).getRGB(), new Color(57, 124, 25).getRGB())),
			Map.entry(Rarity.RARE, Pair.of(new Color(6, 57, 234).getRGB(), new Color(15, 40, 128).getRGB())),
			Map.entry(Rarity.EPIC, Pair.of(new Color(110, 6, 171).getRGB(), new Color(73, 23, 101).getRGB())),
			Map.entry(Rarity.LEGENDARY, Pair.of(new Color(236, 156, 5).getRGB(), new Color(173, 95, 0).getRGB())),
			Map.entry(Rarity.MYTHIC, Pair.of(new Color(219, 0, 255).getRGB(), new Color(155, 4, 245).getRGB())),
			Map.entry(Rarity.SPECIAL, Pair.of(new Color(250, 0, 0).getRGB(), new Color(161, 0, 0).getRGB())),
			Map.entry(Rarity.VERY_SPECIAL, Pair.of(new Color(250, 0, 0).getRGB(), new Color(161, 0, 0).getRGB())),
			Map.entry(Rarity.DIVINE, Pair.of(new Color(0, 241, 248).getRGB(), new Color(1, 178, 154).getRGB())),
			Map.entry(Rarity.ULTIMATE, Pair.of(new Color(115, 0, 0).getRGB(), new Color(77, 0, 0).getRGB()))
	);

	public TooltipDecoratorFeature() {
		ItemRenderEvents.POST_TOOLTIP.register(this::onRenderTooltip);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().uiAndVisuals.toolTipDecorator.enabled;
	}

	@EventHandler(event = "ItemRenderEvents.POST_TOOLTIP")
	private void onRenderTooltip(
			DrawContext context,
			ItemStack itemStack,
			int x, int y,
			int width, int height,
			TextRenderer textRenderer,
			List<TooltipComponent> components
	) {
		if (CLIENT.player == null || CLIENT.world == null) return;
		if (!isEnabled()) return;
		if (itemStack == null || itemStack.isEmpty()) return;

		Rarity rarity = SkyBlockAPI.getRarity(itemStack);
		if (rarity != Rarity.UNKNOWN) {
			if (!RARITY_COLORS.containsKey(rarity)) {
				return;
			}

			Pair<Integer, Integer> colors = RARITY_COLORS.get(rarity);
			if (colors != null) {
				drawBorder(context, x, y, width, height, colors);
			}
		}
	}

	private void drawBorder(DrawContext context, int x, int y, int width, int height, Pair<Integer, Integer> colors) {
		context.getMatrices().pushMatrix();

		GuiRenderer.submitGradientRect(context,
				400,
				x - 3,
				y - 3 + 1,
				x - 3 + 1,
				y + height + 3 - 1,
				colors.left(), colors.right());

		GuiRenderer.submitGradientRect(context,
				400,
				x + width + 2,
				y - 3 + 1,
				x + width + 3,
				y + height + 3 - 1,
				colors.left(), colors.right());

		GuiRenderer.submitGradientRect(context,
				400,
				x - 3,
				y - 3,
				x + width + 3,
				y - 3 + 1,
				colors.left(), colors.left());

		GuiRenderer.submitGradientRect(context,
				400,
				x - 3,
				y + height + 2,
				x + width + 3,
				y + height + 3,
				colors.right(), colors.right());

		context.getMatrices().popMatrix();
	}
}
