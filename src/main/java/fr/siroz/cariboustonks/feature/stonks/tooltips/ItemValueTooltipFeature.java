package fr.siroz.cariboustonks.feature.stonks.tooltips;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.skyblock.item.SkyblockItemStack;
import fr.siroz.cariboustonks.skyblock.item.calculator.ItemValueCalculator;
import fr.siroz.cariboustonks.skyblock.item.calculator.ItemValueResult;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.system.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.system.container.tooltip.ContainerTooltipAppender;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemValueTooltipFeature extends Feature implements ContainerMatcherTrait, ContainerTooltipAppender {

	private final Cache<@NotNull String, @NotNull ItemValueResult> cache = CacheBuilder.newBuilder()
			.maximumSize(555)
			.expireAfterWrite(2, TimeUnit.MINUTES)
			.build();

	private final BooleanSupplier configUseNetworth = () -> ConfigManager.getConfig().general.stonks.useNetworthItemValue;

	private final Set<String> failedCalculations = new HashSet<>();
	private final int priority;

	public ItemValueTooltipFeature(int priority) {
		this.priority = priority;
		TickScheduler.getInstance().runRepeating(failedCalculations::clear, 5, TimeUnit.MINUTES);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().general.stonks.itemValueTooltip;
	}

	@Override
	public @Nullable Pattern getTitlePattern() {
		return null;
	}

	@Override
	public void appendToTooltip(@Nullable Slot focusedSlot, @NotNull ItemStack item, @NotNull List<Component> lines) {
		String uuid = SkyBlockAPI.getSkyBlockItemUuid(item);
		if (uuid.isEmpty()) {
			return;
		}

		try {
			ItemValueResult cached = cache.getIfPresent(uuid);
			if (cached != null) {
				displayItemValue(lines, cached);
				return;
			}

			if (failedCalculations.contains(uuid)) {
				return;
			}

			ItemValueResult result = ItemValueCalculator.getInstance().calculateValue(SkyblockItemStack.of(item), configUseNetworth.getAsBoolean());
			if (result.state() == ItemValueResult.State.SUCCESS) {
				cache.put(uuid, result);
				displayItemValue(lines, result);
			} else {
				failedCalculations.add(uuid);
				if (failedCalculations.size() > 55) {
					failedCalculations.clear();
				}
			}
		} catch (Exception ex) {
			failedCalculations.add(uuid);
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.error("[ItemValueTooltipFeature] An error occured while appending item value tooltip", ex);
			}
		}
	}

	@Override
	public int getPriority() {
		return priority;
	}

	private void displayItemValue(@NotNull List<Component> lines, @Nullable ItemValueResult result) {
		if (result == null || result.calculations().isEmpty()) {
			return;
		}

		double price = result.price();
		if (price > 0) {
			lines.add(Component.literal("Est. Item Value: ").withStyle(ChatFormatting.YELLOW)
					.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(price)).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
					.append(Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(price)).withStyle(ChatFormatting.GOLD))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY))
			);
		}
	}
}
