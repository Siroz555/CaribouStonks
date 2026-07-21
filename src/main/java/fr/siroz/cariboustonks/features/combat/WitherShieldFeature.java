package fr.siroz.cariboustonks.features.combat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.TextHud;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.Modifiers;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import java.text.DecimalFormat;
import java.util.function.BooleanSupplier;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class WitherShieldFeature extends Feature {
	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_shield");
	private static final int ABSORPTION_COOLDOWN_TICKS = 5 * 20 + 2; // 5s - +2, car il y a un petit jeu entre les 2 états.
	private static final int READY_DISPLAY_TICKS = 2 * 20; // 2s
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

	private final BooleanSupplier configOnlyShowTimer =
			() -> this.config().combat.witherShield.onlyShowTimer;

	private int abilityTicksRemaining = -1; // -1 = not active
	private int cooldownTicksRemaining = -1; // -1 = no cooldown
	private int readyTicksRemaining = -1; // -1 = not showing READY

	public WitherShieldFeature() {
		UseItemCallback.EVENT.register(this::onUseItem);
		NetworkEvents.SERVER_TICK.register(this::onServerTick);

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(HUD_ID)
				.hud(new TextHud(
						Component.literal("§5Wither Shield: §e3.4s"),
						this::getText,
						this.config().combat.witherShield.hud,
						50,
						100
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock(); // non-config-check :: hud config
	}

	@Override
	protected void onClientJoinServer() {
		abilityTicksRemaining = -1;
		cooldownTicksRemaining = -1;
		readyTicksRemaining = -1;
	}

	@EventHandler(event = "UseItemCallback.EVENT")
	private InteractionResult onUseItem(Player player, Level _level, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (isEnabled() && !stack.isEmpty() && hasWitherShieldScroll(stack)) {
			if (cooldownTicksRemaining <= 0) {
				abilityTicksRemaining = ABSORPTION_COOLDOWN_TICKS;
				cooldownTicksRemaining = ABSORPTION_COOLDOWN_TICKS;
				readyTicksRemaining = -1;
			}
		}
		return InteractionResult.PASS;
	}

	@EventHandler(event = "NetworkEvents.SERVER_TICK")
	private void onServerTick() {
		if (!isEnabled()) return;
		if (abilityTicksRemaining <= 0 && cooldownTicksRemaining <= 0 && readyTicksRemaining <= 0) return;

		if (abilityTicksRemaining > 0) {
			abilityTicksRemaining--;
			// L'ability vient d'expirer -> affichage READY
			if (abilityTicksRemaining == 0) readyTicksRemaining = READY_DISPLAY_TICKS;
		}

		if (cooldownTicksRemaining > 0) cooldownTicksRemaining--;
		if (readyTicksRemaining > 0) readyTicksRemaining--;
	}

	private Component getText() {
		if (abilityTicksRemaining == -1L && cooldownTicksRemaining == -1L && readyTicksRemaining == -1L) {
			return Component.empty();
		}

		if (abilityTicksRemaining > 0) {
			double timeRemaining = abilityTicksRemaining / 20.0;
			Component timer = Component.literal(DECIMAL_FORMAT.format(timeRemaining) + "s")
					.withColor(this.config().combat.witherShield.timerColor.getRGB());

			return configOnlyShowTimer.getAsBoolean()
					? Component.empty().append(timer)
					: Component.empty()
					  .append(Component.literal("Wither Shield: ").withStyle(ChatFormatting.DARK_PURPLE))
					  .append(timer);
		}

		if (readyTicksRemaining > 0) {
			Component ready = Component.literal(this.config().combat.witherShield.readyMessage);

			return configOnlyShowTimer.getAsBoolean()
					? Component.empty().append(ready)
					: Component.empty()
					  .append(Component.literal("Wither Shield: ").withStyle(ChatFormatting.DARK_PURPLE))
					  .append(ready);
		}

		return Component.empty();
	}

	private boolean hasWitherShieldScroll(@NonNull ItemStack stack) {
		CompoundTag customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (customData.isEmpty()) return false;

		Modifiers modifiers = Modifiers.ofNbt(customData);
		if (modifiers.abilityScrolls().isEmpty()) return false;

		return modifiers.abilityScrolls().get().contains("WITHER_SHIELD_SCROLL");
	}
}
