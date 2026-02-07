package fr.siroz.cariboustonks.feature.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.ReminderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.core.model.TimedObjectModel;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.system.ReminderSystem;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.StonksUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class EnchantedCloakReminderFeature extends Feature {

	private static final Pattern CHAT_BOOST_USED_PATTERN = Pattern.compile(
			"TIME WARP! You have successfully warped time for your (?<type>.+?)!");

	private static final String REMINDER_TYPE = "ENCHANTED_CLOAK";

	private static final ItemStack ICON = new ItemStack(Items.CLOCK);

	public EnchantedCloakReminderFeature() {
		ChatEvents.MESSAGE_RECEIVED.register(this::onChatMessage);

		this.addComponent(ReminderComponent.class, ReminderComponent.builder(REMINDER_TYPE)
				.display(getReminderDisplay())
				.onExpire(this::onReminderExpire)
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() != IslandType.DUNGEON
				&& this.config().general.reminders.enchantedCloak;
	}

	private @NonNull ReminderDisplay getReminderDisplay() {
		return ReminderDisplay.of(
				Component.literal("Enchanted Cloak").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD, ChatFormatting.UNDERLINE),
				null,
				ICON
		);
	}

	private void onReminderExpire(@NonNull TimedObjectModel timedObject) {
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
		if (this.config().general.reminders.playSound) {
			Client.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1f);
		}
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVED")
	private void onChatMessage(@NonNull Component text) {
		if (!isEnabled()) {
			return;
		}

		Matcher chatBoostTypeMatcher = CHAT_BOOST_USED_PATTERN.matcher(text.getString());
		if (chatBoostTypeMatcher.matches()) {

			String type = chatBoostTypeMatcher.group("type");
			BoostType boostType = BoostType.getById(type);
			if (boostType != null) {

				String message = StonksUtils.textToJson(boostType.name).orElse(boostType.name.getString());

				TimedObjectModel timedObject = new TimedObjectModel(
						"cloak::" + boostType.name(),
						message,
						Instant.now().plus(Duration.ofHours(48)),
						REMINDER_TYPE);

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

		static @Nullable BoostType getById(@NonNull String id) {
			return Arrays.stream(values())
					.filter(boostType -> boostType.id.equals(id))
					.findFirst()
					.orElse(null);
		}

		static @Nullable BoostType getByName(@NonNull String name) {
			return Arrays.stream(values())
					.filter(boostType -> boostType.name().equalsIgnoreCase(name))
					.findFirst()
					.orElse(null);
		}
	}
}
