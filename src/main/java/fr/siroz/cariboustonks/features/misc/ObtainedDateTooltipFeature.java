package fr.siroz.cariboustonks.features.misc;

import fr.siroz.cariboustonks.core.component.TooltipAppenderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.gui.MatcherTrait;
import fr.siroz.cariboustonks.util.ItemUtil;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ObtainedDateTooltipFeature extends Feature {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM d, yyyy")
			.withZone(ZoneId.systemDefault())
			.localizedBy(Locale.ENGLISH);
	private static final DateTimeFormatter OLD_FORMATTER = DateTimeFormatter.ofPattern("M/d/yy h:m a")
			.withZone(ZoneId.of("UTC"))
			.localizedBy(Locale.ENGLISH);

	public ObtainedDateTooltipFeature(int priority) {
		this.addComponent(TooltipAppenderComponent.class, TooltipAppenderComponent.builder()
				.priority(priority)
				.trait(MatcherTrait.empty())
				.appender((_, item, lines) -> {
					String timestamp = getTimestamp(item);
					if (timestamp == null) return;

					lines.add(Component.empty()
							.append(Component.literal("Obtained: ").withStyle(ChatFormatting.GRAY))
							.append(Component.literal(timestamp).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
				})
				.build());
	}

	@Override
	public boolean isEnabled() {
		return this.skyBlock().location().onSkyBlock() && this.config().misc.showObtainedDateTooltip;
	}

	private static @Nullable String getTimestamp(ItemStack item) {
		try {
			TemporalAccessor extracted = extractTimestamp(item);
			return extracted != null ? FORMATTER.format(extracted) : null;
		} catch (Exception _) {
			return null;
		}
	}

	private static TemporalAccessor extractTimestamp(ItemStack stack) {
		CompoundTag customData = ItemUtil.getCustomData(stack);
		if (customData.getString("uuid").isEmpty()) return null;
		if (customData.get("timestamp") instanceof LongTag(long value)) return Instant.ofEpochMilli(value);
		if (customData.get("timestamp") instanceof StringTag(String value)) return OLD_FORMATTER.parse(value);
		return null;
	}
}
