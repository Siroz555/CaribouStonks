package fr.siroz.cariboustonks.feature.reminders;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.reminder.Reminder;
import fr.siroz.cariboustonks.manager.reminder.ReminderDisplay;
import fr.siroz.cariboustonks.manager.reminder.ReminderManager;
import fr.siroz.cariboustonks.manager.reminder.TimedObject;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.render.notification.Notification;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
                Text.literal("Enchanted Cloak").formatted(Formatting.YELLOW, Formatting.BOLD, Formatting.UNDERLINE),
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

        Text text = Text.empty().append(boostType.getName())
                .append(Text.literal(" is ready!").formatted(Formatting.GREEN));

        Client.sendMessageWithPrefix(Text.literal("[Enchanted Cloak] ").formatted(Formatting.GOLD)
                .append(text));

        Notification.show(Text.literal("Enchanted Cloak !\n").formatted(Formatting.GOLD, Formatting.BOLD)
                        .append(text), ICON);
    }

    private void onChatMessage(@NotNull Text text) {
        if (!isEnabled()) {
			return;
		}

        Matcher chatBoostTypeMatcher = CHAT_BOOST_USED_PATTERN.matcher(text.getString());
        if (chatBoostTypeMatcher.matches()) {

            String type = chatBoostTypeMatcher.group("type");
            BoostType boostType = BoostType.getById(type);
            if (boostType != null) {

                String message = StonksUtils.textToJson(boostType.getName()).orElse(boostType.getName().getString());

                TimedObject timedObject = new TimedObject(
                        "cloak::" + boostType.name(),
                        message,
                        Instant.now().plus(Duration.ofHours(48)),
                        reminderType());

                CaribouStonks.managers()
                        .getManager(ReminderManager.class)
                        .addTimedObject(timedObject);
            }
        }
    }

    public enum BoostType {
        MINIONS("minions", Text.literal("Minions").formatted(Formatting.AQUA)),
        CHOCOLATE_FACTORY("Chocolate Factory", Text.literal("Chocolate Factory").formatted(Formatting.GOLD)),
        PET_TRAINING("Pet Training", Text.literal("Pet Training").formatted(Formatting.LIGHT_PURPLE)),
        PET_SITTER("Pet Sitter", Text.literal("Pet Sitter").formatted(Formatting.RED)),
        AGING_ITEMS("aging items", Text.literal("Aging Items").formatted(Formatting.YELLOW)),
        FORGE("Forge", Text.literal("Forge").formatted(Formatting.GOLD)),
        ;

        private final String id;
        private final Text name;

        BoostType(String id, Text name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public Text getName() {
            return name;
        }

        public static @Nullable BoostType getById(@NotNull String id) {
            return Arrays.stream(values())
                    .filter(boostType -> boostType.id.equals(id))
                    .findFirst()
                    .orElse(null);
        }

        public static @Nullable BoostType getByName(@NotNull String name) {
            return Arrays.stream(values())
                    .filter(boostType -> boostType.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(null);
        }
    }
}
