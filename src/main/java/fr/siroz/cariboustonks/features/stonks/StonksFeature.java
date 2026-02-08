package fr.siroz.cariboustonks.features.stonks;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.KeybindComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.input.KeyBind;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.screens.stonks.StonksScreen;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import fr.siroz.cariboustonks.util.NotEnoughUpdatesUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

public class StonksFeature extends Feature {

	public StonksFeature() {
		this.addComponent(KeybindComponent.class, KeybindComponent.builder()
				.add(new KeyBind("Stonks Item", GLFW.GLFW_KEY_K, this::onKeyPressed))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	public void stonksItem(@NonNull ItemStack stack) {
		Client.sendMessageWithPrefix(stack.getHoverName().copy().append(Component.literal(" ..")));

		String neuId = NotEnoughUpdatesUtils.getNeuId(stack);
		String hypixelSkyBlockId = SkyBlockAPI.getSkyBlockApiId(stack);

		if (neuId.isEmpty() || hypixelSkyBlockId.isEmpty()) {

			String neuIdError = neuId.isEmpty() ? "EMPTY" : neuId;
			String hypixelSkyBlockIdError = hypixelSkyBlockId.isEmpty() ? "EMPTY" : hypixelSkyBlockId;

			Client.sendErrorMessage("Unable to identify item " + stack.getHoverName().getString(), false);
			if (DeveloperTools.isInDevelopment()) {
				String extra = "(id: " + neuIdError + " | skyBlockApiId: " + hypixelSkyBlockIdError + ")";
				Client.sendMessage(Component.literal(extra).withStyle(ChatFormatting.DARK_GRAY));
			}

			CaribouStonks.LOGGER.error("[StonksFeature] Unable to identify ItemStack IDs. Minecraft ItemStack: " +
							"{} NEU ID: {}, Hypixel SkyBlock API ID: {}",
					stack.getHoverName().getString(), neuIdError, hypixelSkyBlockIdError);
		} else {
			Minecraft.getInstance().setScreen(StonksScreen.create(ItemLookupKey.of(neuId, hypixelSkyBlockId)));
		}
	}

	private void onKeyPressed(Screen screen, Slot slot) {
		if (isEnabled() && !slot.getItem().isEmpty()) {
			stonksItem(slot.getItem());
		}
	}
}
