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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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
			if (itemStack == null || itemStack.isOf(Items.BLACK_STAINED_GLASS_PANE)) {
				continue;
			}

			if (isContact(itemStack)) {
				String name = StonksUtils.stripColor(itemStack.getName().getString());
				if (ConfigManager.getConfig().uiAndVisuals.favoriteAbiphoneContacts.contains(name)) {
					highlights.add(ColorHighlight.yellow(entry.getIntKey(), 0.25f));
				}
			}
		}

		return highlights;
	}

	@Override
	public void appendToTooltip(@Nullable Slot focusedSlot, @NotNull ItemStack item, @NotNull List<Text> lines) {
		if (isContact(item)) {
			String name = StonksUtils.stripColor(item.getName().getString());
			if (ConfigManager.getConfig().uiAndVisuals.favoriteAbiphoneContacts.contains(name)) {
				lines.add(Text.literal("SHIFT").formatted(Formatting.YELLOW, Formatting.BOLD)
						.append(Text.literal(" To remove from favourite contacts").formatted(Formatting.YELLOW)));
			} else {
				lines.add(Text.literal("SHIFT").formatted(Formatting.YELLOW, Formatting.BOLD)
						.append(Text.literal(" To add to favourite contacts").formatted(Formatting.YELLOW)));
			}

			lines.add(Text.literal("(from CaribouStonks)").formatted(Formatting.DARK_GRAY));
		}
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@EventHandler(event = "CustomScreenEvents.KEY_PRESSED")
	private void onKeyPressed(Screen screen, KeyInput input, @NotNull Slot slot) {
		if (!isEnabled() || screen == null) return;
		if (!matches(screen)) return;

		if (StonksUtils.hasShiftDown() && COOLDOWN.test()) {
			ItemStack itemStack = slot.getStack();
			if (itemStack == null) {
				return;
			}

			if (isContact(itemStack)) {
				String name = StonksUtils.stripColor(itemStack.getName().getString());
				toggleFavouriteContact(itemStack.getName(), name);
			}
		}
	}

	private void onClose(Screen screen) {
		if (matches(screen) && updated) {
			updated = false;
			ConfigManager.saveConfig();
		}
	}

	private boolean isContact(ItemStack itemStack) {
		return ItemUtils.getLore(itemStack).stream()
				.map(Text::getString)
				.anyMatch(s -> s.equals("Left-click to call!") || s.equals("Click to call!"));
	}

	private void toggleFavouriteContact(Text rawContactName, String contactName) {
		updated = true;
		if (ConfigManager.getConfig().uiAndVisuals.favoriteAbiphoneContacts.contains(contactName)) {
			ConfigManager.getConfig().uiAndVisuals.favoriteAbiphoneContacts.remove(contactName);
			Client.sendMessageWithPrefix(Text.literal("Removed ").formatted(Formatting.RED)
					.append(rawContactName)
					.append(" from favourite contacts.").formatted(Formatting.RED));
			Client.playSound(SoundEvents.ENTITY_VILLAGER_NO, 1f, 1f);
		} else {
			ConfigManager.getConfig().uiAndVisuals.favoriteAbiphoneContacts.add(contactName);
			Client.sendMessageWithPrefix(Text.literal("Added ").formatted(Formatting.GREEN)
					.append(rawContactName)
					.append(" to favourite contacts.").formatted(Formatting.GREEN));
			Client.playSound(SoundEvents.ENTITY_VILLAGER_YES, 1f, 1f);
		}
	}
}
