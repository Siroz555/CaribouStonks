package fr.siroz.cariboustonks.feature.ui;

import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.component.ContainerMatcherComponent;
import fr.siroz.cariboustonks.core.component.ContainerOverlayComponent;
import fr.siroz.cariboustonks.core.component.TooltipAppenderComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.CustomScreenEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.cooldown.Cooldown;
import fr.siroz.cariboustonks.util.render.gui.ColorHighlight;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

public class AbiphoneFavoriteContactFeature extends Feature {

	private static final Pattern TITLE_PATTERN = Pattern.compile("^Abiphone.*");
	private static final Cooldown COOLDOWN = Cooldown.of(1, TimeUnit.SECONDS);

	private boolean updated = false;

	public AbiphoneFavoriteContactFeature(int priority) {
		CustomScreenEvents.KEY_PRESSED.register(this::onKeyPressed);
		CustomScreenEvents.CLOSE.register(this::onClose);

		this.addComponent(ContainerMatcherComponent.class, ContainerMatcherComponent.of(TITLE_PATTERN));
		this.addComponent(TooltipAppenderComponent.class, TooltipAppenderComponent.builder()
				.priority(priority)
				.appender((focusedSlot, item, lines) -> {
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
				})
				.build());
		this.addComponent(ContainerOverlayComponent.class, ContainerOverlayComponent.builder()
				.content(slots -> {
					List<ColorHighlight> highlights = new ArrayList<>();
					slots.forEach((slotIndex, itemStack) -> {
						if (isContact(itemStack)) {
							String name = StonksUtils.stripColor(itemStack.getHoverName().getString());
							if (ConfigManager.getConfig().uiAndVisuals.favoriteAbiphoneContacts.contains(name)) {
								highlights.add(ColorHighlight.yellow(slotIndex, 0.25f));
							}
						}
					});
					return highlights;
				})
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	@EventHandler(event = "CustomScreenEvents.KEY_PRESSED")
	private void onKeyPressed(Screen screen, KeyEvent input, @NotNull Slot slot) {
		if (!isEnabled() || screen == null) return;
		if (isMatcherBlocking(screen)) return;

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
		if (!isMatcherBlocking(screen) && updated) {
			updated = false;
			ConfigManager.saveConfig();
		}
	}

	private boolean isMatcherBlocking(Screen screen) {
		return !this.getComponent(ContainerMatcherComponent.class)
				.map(matcherComponent -> matcherComponent.matches(screen, 0))
				.orElse(false);
	}

	private boolean isContact(ItemStack itemStack) {
		if (itemStack == null || itemStack.is(Items.BLACK_STAINED_GLASS_PANE)) return false;

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
