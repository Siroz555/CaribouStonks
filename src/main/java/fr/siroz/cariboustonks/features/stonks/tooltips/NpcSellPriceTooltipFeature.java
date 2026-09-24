package fr.siroz.cariboustonks.features.stonks.tooltips;

import fr.siroz.cariboustonks.core.component.TooltipAppenderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.gui.MatcherTrait;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.SkyBlockItemData;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockItems;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.List;
import java.util.OptionalDouble;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class NpcSellPriceTooltipFeature extends Feature {

	public NpcSellPriceTooltipFeature(int priority) {
		this.addComponent(TooltipAppenderComponent.class, TooltipAppenderComponent.builder()
				.priority(priority)
				.trait(MatcherTrait.empty())
				.appender(this::appendToTooltip)
				.build());
	}

	@Override
	public boolean isEnabled() {
		return this.skyBlock().location().onSkyBlock() && this.config().general.stonks.npcTooltipPrice;
	}

	private void appendToTooltip(@Nullable Slot focusedSlot, @NonNull ItemStack item, @NonNull List<Component> lines) {
		String skyBlockItemId = SkyBlockItems.getSkyBlockItemId(item);
		SkyBlockItemData skyBlockItemData = this.skyBlock().getHypixelDataSource().getSkyBlockItem(skyBlockItemId);
		if (skyBlockItemData == null) return;

		OptionalDouble npcSellPrice = skyBlockItemData.npcSellPrice();
		if (npcSellPrice.isPresent() && npcSellPrice.getAsDouble() > 0) {
			addNpcLine(lines, npcSellPrice.getAsDouble(), item.getCount());
		}
	}

	private void addNpcLine(@NonNull List<Component> lines, double unitValue, int count) {
		double totalValue = unitValue * count;

		String totalDisplay = StonksUtils.INTEGER_NUMBERS.format(totalValue);
		String unitDisplay = StonksUtils.INTEGER_NUMBERS.format(unitValue);
		if (totalValue < 100_000) {
			totalDisplay = StonksUtils.FLOAT_NUMBERS.format(totalValue);
			unitDisplay = StonksUtils.FLOAT_NUMBERS.format(unitValue);
		}

		MutableComponent line = Component.literal("NPC Price: ").withStyle(ChatFormatting.YELLOW)
				.append(Component.literal(totalDisplay).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" Coins").withStyle(ChatFormatting.GOLD));

		if (count > 1) {
			line.append(Component.literal(" (").withStyle(ChatFormatting.DARK_GRAY))
					.append(Component.literal(unitDisplay + " each").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(")").withStyle(ChatFormatting.DARK_GRAY));
		}

		lines.add(line);
	}
}
