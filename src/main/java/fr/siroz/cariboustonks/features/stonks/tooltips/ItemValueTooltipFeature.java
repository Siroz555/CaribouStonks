package fr.siroz.cariboustonks.features.stonks.tooltips;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ContainerMatcherComponent;
import fr.siroz.cariboustonks.core.component.TooltipAppenderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.item.SkyblockItemStack;
import fr.siroz.cariboustonks.core.skyblock.item.calculator.ItemValueCalculator;
import fr.siroz.cariboustonks.core.skyblock.item.calculator.ItemValueResult;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ItemValueTooltipFeature extends Feature {

	private final Cache<String, ItemValueResult> cache = CacheBuilder.newBuilder()
			.maximumSize(555)
			.expireAfterWrite(2, TimeUnit.MINUTES)
			.build();

	private final BooleanSupplier configUseNetworth =
			() -> this.config().general.stonks.useNetworthItemValue;

	private final Set<String> failedCalculations = new HashSet<>();

	public ItemValueTooltipFeature(int priority) {
		TickScheduler.getInstance().runRepeating(failedCalculations::clear, 5, TimeUnit.MINUTES);

		this.addComponent(ContainerMatcherComponent.class, ContainerMatcherComponent.empty());
		this.addComponent(TooltipAppenderComponent.class, TooltipAppenderComponent.builder()
				.priority(priority)
				.appender(this::appendToTooltip)
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().general.stonks.itemValueTooltip;
	}

	private void appendToTooltip(@Nullable Slot focusedSlot, @NonNull ItemStack item, @NonNull List<Component> lines) {
		String uuid = SkyBlockAPI.getSkyBlockItemUuid(item);
		if (uuid.isEmpty()) return;

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

	private void displayItemValue(@NonNull List<Component> lines, @Nullable ItemValueResult result) {
		if (result == null || result.calculations().isEmpty()) return;

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
