package fr.siroz.cariboustonks.feature.stonks;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.util.ItemLookupKey;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.manager.keybinds.KeyBind;
import fr.siroz.cariboustonks.manager.keybinds.KeyBindComponent;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.NotEnoughUpdatesUtils;
import java.util.Collections;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

public class StonksFeature extends Feature {

	public StonksFeature() {
		addComponent(KeyBindComponent.class, () -> Collections.singletonList(
				new KeyBind("Stonks Item", GLFW.GLFW_KEY_K, this::onKeyPressed)
		));
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	private void onKeyPressed(Screen screen, Slot slot) {
		if (isEnabled() && slot.getStack() != null && !slot.getStack().isEmpty()) {
			stonksItem(slot.getStack());
		}
	}

	public void stonksItem(@NotNull ItemStack stack) {
		Client.sendMessageWithPrefix(stack.getName().copy().append(Text.literal(" ..")));

		String neuId = NotEnoughUpdatesUtils.getNeuId(stack);
		String hypixelSkyBlockId = SkyBlockAPI.getSkyBlockApiId(stack);

		if (neuId.isEmpty() || hypixelSkyBlockId.isEmpty()) {

			String neuIdError = neuId.isEmpty() ? "EMPTY" : neuId;
			String hypixelSkyBlockIdError = hypixelSkyBlockId.isEmpty() ? "EMPTY" : hypixelSkyBlockId;

			Client.sendErrorMessage("Unable to identify item " + stack.getName().getString(), false);
			if (DeveloperTools.isInDevelopment()) {
				String extra = "(id: " + neuIdError + " | skyBlockApiId: " + hypixelSkyBlockIdError + ")";
				Client.sendMessage(Text.literal(extra).formatted(Formatting.DARK_GRAY));
			}

			CaribouStonks.LOGGER.error("[StonksFeature] Unable to identify ItemStack IDs. Minecraft ItemStack: " +
							"{} NEU ID: {}, Hypixel SkyBlock API ID: {}",
					stack.getName().getString(), neuIdError, hypixelSkyBlockIdError);
		} else {
			MinecraftClient.getInstance().setScreen(StonksScreen.create(ItemLookupKey.of(neuId, hypixelSkyBlockId)));
		}
	}
}
