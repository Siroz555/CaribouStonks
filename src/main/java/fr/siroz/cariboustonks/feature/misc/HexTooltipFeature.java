package fr.siroz.cariboustonks.feature.misc;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.system.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.system.container.tooltip.ContainerTooltipAppender;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HexTooltipFeature extends Feature implements ContainerMatcherTrait, ContainerTooltipAppender {

	private final int priority;

	public HexTooltipFeature(int priority) {
		this.priority = priority;
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().misc.showHexOnDyedItemEverywhere;
	}

	@Override
	public @Nullable Pattern getTitlePattern() {
		return null;
	}

	@Override
	public void appendToTooltip(@Nullable Slot _slot, @NotNull ItemStack item, @NotNull List<Component> lines) {
		DyedItemColor dyedColor = item.get(DataComponents.DYED_COLOR);
		if (dyedColor == null) return;

		int dyeColor = dyedColor.rgb() & 0x00FFFFFF; // ARGB possible > mask
		String colorHex = String.format("%06X", dyeColor);

		lines.add(1, Component.empty()
				.append(Component.literal("Hex: ").withStyle(ChatFormatting.DARK_GRAY))
				.append(Component.literal("#" + colorHex).withColor(Integer.decode("0x" + colorHex)))
		);
	}

	@Override
	public int getPriority() {
		return priority;
	}
}
