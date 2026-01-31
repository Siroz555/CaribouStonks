package fr.siroz.cariboustonks.feature.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.system.reminder.Reminder;
import fr.siroz.cariboustonks.system.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.system.reminder.ReminderSystem;
import fr.siroz.cariboustonks.system.reminder.TimedObject;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.skyblock.IslandType;
import fr.siroz.cariboustonks.util.StonksUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EnchantedCloakReminderFeature extends Feature implements Reminder {

	private static final Pattern CHAT_BOOST_USED_PATTERN = Pattern.compile(
			"TIME WARP! You have successfully warped time for your (?<type>.+?)!");

	private static final ItemStack ICON = new ItemStack(Items.CLOCK);

	public EnchantedCloakReminderFeature() {
		ChatEvents.MESSAGE_RECEIVED.register(this::onChatMessage);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() != IslandType.DUNGEON
				&& ConfigManager.getConfig().general.reminders.enchantedCloak;
	}

	@Override
	public @NotNull String reminderType() {
		return "ENCHANTED_CLOAK";
	}

	@Override
	public @NotNull ReminderDisplay display() {
		return ReminderDisplay.of(
				Component.literal("Enchanted Cloak").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
				null,
				ICON
		);
	}

	@Override
	public void onExpire(@NotNull TimedObject timedObject) {
		BoostType boostType = BoostType.getByName(timedObject.id().replace("cloak::", ""));
		if (boostType == null) {
			return;
		}

		MutableComponent text = Component.empty()
				.append(boostType.name)
				.append(Component.literal(" is ready!").withStyle(ChatFormatting.GREEN));
		MutableComponent message = Component.empty()
				.append(Component.literal("[Enchanted Cloak] ").withStyle(ChatFormatting.GOLD))
				.append(text);
		MutableComponent notification = Component.empty()
				.append(Component.literal("Enchanted Cloak !").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
				.append(Component.literal("\n"))
				.append(text);

		Client.sendMessageWithPrefix(message);
		Client.showNotification(notification, ICON);
		if (ConfigManager.getConfig().general.reminders.playSound) {
			Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
		}
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onChatMessage(@NotNull Component text) {
		if (!isEnabled()) {
			return;
		}

		Matcher chatBoostTypeMatcher = CHAT_BOOST_USED_PATTERN.matcher(text.getString());
		if (chatBoostTypeMatcher.matches()) {

			String type = chatBoostTypeMatcher.group("type");
			BoostType boostType = BoostType.getById(type);
			if (boostType != null) {

				String message = StonksUtils.textToJson(boostType.name).orElse(boostType.name.getString());

				TimedObject timedObject = new TimedObject(
						"cloak::" + boostType.name(),
						message,
						Instant.now().plus(Duration.ofHours(48)),
						reminderType());

				CaribouStonks.systems()
						.getSystem(ReminderSystem.class)
						.addTimedObject(timedObject);
			}
		}
	}

	private enum BoostType {
		MINIONS("Minions", Component.literal("Minions").withStyle(ChatFormatting.AQUA)),
		CHOCOLATE_FACTORY("Chocolate Factory", Component.literal("Chocolate Factory").withStyle(ChatFormatting.GOLD)),
		PET_TRAINING("Pet Training", Component.literal("Pet Training").withStyle(ChatFormatting.LIGHT_PURPLE)),
		PET_SITTER("Pet Sitter", Component.literal("Pet Sitter").withStyle(ChatFormatting.RED)),
		AGING_ITEMS("Aging Items", Component.literal("Aging Items").withStyle(ChatFormatting.YELLOW)),
		FORGE("Forges", Component.literal("Forge").withStyle(ChatFormatting.GOLD)),
		;

		private final String id;
		private final Component name;

		BoostType(String id, Component name) {
			this.id = id;
			this.name = name;
		}

		static @Nullable BoostType getById(@NotNull String id) {
			return Arrays.stream(values())
					.filter(boostType -> boostType.id.equals(id))
					.findFirst()
					.orElse(null);
		}

		static @Nullable BoostType getByName(@NotNull String name) {
			return Arrays.stream(values())
					.filter(boostType -> boostType.name().equalsIgnoreCase(name))
					.findFirst()
					.orElse(null);
		}
	}
}
