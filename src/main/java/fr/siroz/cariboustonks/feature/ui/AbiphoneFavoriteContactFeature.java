package fr.siroz.cariboustonks.feature.ui;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.CustomScreenEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.container.ContainerMatcherTrait;
import fr.siroz.cariboustonks.manager.container.overlay.ContainerOverlay;
import fr.siroz.cariboustonks.manager.container.tooltip.ContainerTooltipAppender;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.cooldown.Cooldown;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.Slot;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class AbiphoneFavoriteContactFeature
		extends Feature
		implements ContainerMatcherTrait, ContainerOverlay, ContainerTooltipAppender {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^Abiphone.*");
	private static final Cooldown COOLDOWN = Cooldown.of(1, TimeUnit.SECONDS);

	private final int priority;
	private boolean updated = false;

	public AbiphoneFavoriteContactFeature(int priority) {
		this.priority = priority;
		CustomScreenEvents.KEY_PRESSED.register(this::onKeyPressed);
		CustomScreenEvents.CLOSE.register(this::onClose);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	@Override
	public @Nullable Pattern getTitlePattern() {
		return TITLE_PATTERN;
	}

	@Override
	public @NotNull List<ColorHighlight> content(@NotNull Int2ObjectMap<ItemStack> slots) {
		List<ColorHighlight> highlights = new ArrayList<>();
		for (Int2ObjectMap.Entry<ItemStack> entry : slots.int2ObjectEntrySet()) {

			ItemStack itemStack = entry.getValue();
			if (itemStack == null || itemStack.is(Items.BLACK_STAINED_GLASS_PANE)) {
				continue;
			}

			if (isContact(itemStack)) {
				String name = StonksUtils.stripColor(itemStack.getHoverName().getString());
				if (ConfigManager.getConfig().uiAndVisuals.favoriteAbiphoneContacts.contains(name)) {
					highlights.add(ColorHighlight.yellow(entry.getIntKey(), 0.25f));
				}
			}
		}

		return highlights;
	}

	@Override
	public void appendToTooltip(@Nullable Slot focusedSlot, @NotNull ItemStack item, @NotNull List<Component> lines) {
		if (isContact(item)) {
			String name = StonksUtils.stripColor(item.getHoverName().getString());
			if (ConfigManager.getConfig().uiAndVisuals.favoriteAbiphoneContacts.contains(name)) {
				lines.add(Component.literal("SHIFT").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
						.append(Component.literal(" To remove from favourite contacts").withStyle(ChatFormatting.YELLOW)));
			} else {
				lines.add(Component.literal("SHIFT").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
						.append(Component.literal(" To add to favourite contacts").withStyle(ChatFormatting.YELLOW)));
			}

			lines.add(Component.literal("(from CaribouStonks)").withStyle(ChatFormatting.DARK_GRAY));
		}
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@EventHandler(event = "CustomScreenEvents.KEY_PRESSED")
	private void onKeyPressed(Screen screen, KeyEvent input, @NotNull Slot slot) {
		if (!isEnabled() || screen == null) return;
		if (!matches(screen)) return;

		if (Client.hasShiftDown() && COOLDOWN.test()) {
			ItemStack itemStack = slot.getItem();
			if (isContact(itemStack)) {
				String name = StonksUtils.stripColor(itemStack.getHoverName().getString());
				toggleFavouriteContact(itemStack.getHoverName(), name);
			}
		}
	}

	@EventHandler(event = "CustomScreenEvents.CLOSE")
	private void onClose(Screen screen) {
		if (matches(screen) && updated) {
			updated = false;
			ConfigManager.saveConfig();
		}
	}

	private boolean isContact(ItemStack itemStack) {
		return ItemUtils.getLore(itemStack).stream()
				.map(Component::getString)
				.anyMatch(s -> s.equals("Left-click to call!") || s.equals("Click to call!"));
	}

	private void toggleFavouriteContact(Component rawContactName, String contactName) {
		updated = true;
		if (ConfigManager.getConfig().uiAndVisuals.favoriteAbiphoneContacts.contains(contactName)) {
			ConfigManager.getConfig().uiAndVisuals.favoriteAbiphoneContacts.remove(contactName);
			Client.sendMessageWithPrefix(Component.literal("Removed ").withStyle(ChatFormatting.RED)
					.append(rawContactName)
					.append(" from favourite contacts.").withStyle(ChatFormatting.RED));
			Client.playSound(SoundEvents.VILLAGER_NO, 1f, 1f);
		} else {
			ConfigManager.getConfig().uiAndVisuals.favoriteAbiphoneContacts.add(contactName);
			Client.sendMessageWithPrefix(Component.literal("Added ").withStyle(ChatFormatting.GREEN)
					.append(rawContactName)
					.append(" to favourite contacts.").withStyle(ChatFormatting.GREEN));
			Client.playSound(SoundEvents.VILLAGER_YES, 1f, 1f);
		}
	}
}
