package fr.siroz.cariboustonks.feature.misc;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.manager.container.tooltip.ContainerTooltipAppender;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
	public void appendToTooltip(@Nullable Slot _slot, @NotNull ItemStack item, @NotNull List<Text> lines) {
		DyedColorComponent dyedColor = item.get(DataComponentTypes.DYED_COLOR);
		if (dyedColor == null) return;

		int dyeColor = dyedColor.rgb() & 0x00FFFFFF; // ARGB possible > mask
		String colorHex = String.format("%06X", dyeColor);

		lines.add(1, Text.empty()
				.append(Text.literal("Hex: ").formatted(Formatting.DARK_GRAY))
				.append(Text.literal("#" + colorHex).withColor(Integer.decode("0x" + colorHex)))
		);
	}

	@Override
	public int getPriority() {
		return priority;
	}
}
