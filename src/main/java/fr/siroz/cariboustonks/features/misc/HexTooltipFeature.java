package fr.siroz.cariboustonks.features.misc;

import fr.siroz.cariboustonks.core.component.ContainerMatcherComponent;
import fr.siroz.cariboustonks.core.component.TooltipAppenderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.component.DyedItemColor;

public class HexTooltipFeature extends Feature {

	public HexTooltipFeature(int priority) {
		this.addComponent(ContainerMatcherComponent.class, ContainerMatcherComponent.empty());
		this.addComponent(TooltipAppenderComponent.class, TooltipAppenderComponent.builder()
				.priority(priority)
				.appender((focusedSlot, item, lines) -> {
					DyedItemColor dyedColor = item.get(DataComponents.DYED_COLOR);
					if (dyedColor == null) return;

					int dyeColor = dyedColor.rgb() & 0x00FFFFFF; // ARGB possible > mask
					String colorHex = String.format("%06X", dyeColor);

					lines.add(1, Component.empty()
							.append(Component.literal("Hex: ").withStyle(ChatFormatting.DARK_GRAY))
							.append(Component.literal("#" + colorHex).withColor(Integer.decode("0x" + colorHex)))
					);
				})
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().misc.showHexOnDyedItemEverywhere;
	}
}
