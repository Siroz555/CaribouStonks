package fr.siroz.cariboustonks.feature.combat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.item.metadata.Modifiers;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.hud.Hud;
import fr.siroz.cariboustonks.manager.hud.HudProvider;
import fr.siroz.cariboustonks.manager.hud.TextHud;
import it.unimi.dsi.fastutil.Pair;
import java.text.DecimalFormat;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class WitherShieldFeature extends Feature implements HudProvider {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_shield");

	private static final long ABSORPTION_COOLDOWN = 5_000L;
	private static final long READY_DISPLAY = 2_000L;
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

	private long abilityEnd = -1L; // not active
	private long cooldownEnd = -1L; // no cooldown
	private long readyUntil = -1L; // not showing READY

	public WitherShieldFeature() {
		UseItemCallback.EVENT.register(this::onUseItem);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock();
	}

	@Override
	protected void onClientJoinServer() {
		abilityEnd = -1;
		cooldownEnd = -1;
		readyUntil = -1;
	}

	@Override
	public @NotNull Pair<Identifier, Identifier> getAttachLayerAfter() {
		return Pair.of(VanillaHudElements.STATUS_EFFECTS, HUD_ID);
	}

	@Override
	public @NotNull Hud getHud() {
		return new TextHud(
				Component.literal("§5Wither Shield: §e3.4s"),
				this::getText,
				ConfigManager.getConfig().combat.witherShield.hud,
				50,
				100
		);
	}

	@EventHandler(event = "UseItemCallback.EVENT")
	private InteractionResult onUseItem(Player player, Level _level, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (isEnabled() && !stack.isEmpty() && stack.is(Items.IRON_SWORD) && hasWitherShieldScroll(stack)) {
			long now = System.currentTimeMillis();
			if (now >= cooldownEnd) {
				abilityEnd = now + ABSORPTION_COOLDOWN;
				cooldownEnd = now + ABSORPTION_COOLDOWN;
				readyUntil = -1L;
			}
		}
		return InteractionResult.PASS;
	}

	private Component getText() {
		if (abilityEnd == -1L && cooldownEnd == -1L && readyUntil == -1L) {
			return Component.empty();
		}

		long now = System.currentTimeMillis();
		// Si ability est active
		if (abilityEnd > now) {
			double timeRemaining = (abilityEnd - now) / 1000.0d;
			Component timer = Component.literal(DECIMAL_FORMAT.format(timeRemaining) + "s")
					.withColor(ConfigManager.getConfig().combat.witherShield.timerColor.getRGB());
			return onlyShowTimer()
					? Component.empty().append(timer)
					: Component.empty()
					.append(Component.literal("Wither Shield: ").withStyle(ChatFormatting.DARK_PURPLE))
					.append(timer);
		}

		// Si abilityEnd était défini mais est maintenant expiré -> READY
		if (abilityEnd != -1L && readyUntil == -1L) {
			// ability vient tout juste d'expirer (transition)
			readyUntil = now + READY_DISPLAY;
		}
		// Clear abilityEnd pour marquer que ability n'est plus active
		abilityEnd = -1L;
		// Afficher READY si dans la fenêtre readyUntil
		if (readyUntil > now) {
			Component ready = Component.literal("READY").withStyle(ChatFormatting.GREEN);
			return onlyShowTimer()
					? Component.empty().append(ready)
					: Component.empty()
					.append(Component.literal("Wither Shield: ").withStyle(ChatFormatting.DARK_PURPLE))
					.append(ready);
		}

		return Component.empty();
	}

	private boolean hasWitherShieldScroll(@NotNull ItemStack stack) {
		CompoundTag customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		if (customData.isEmpty()) {
			return false;
		}

		Modifiers modifiers = Modifiers.ofNbt(customData);
		if (modifiers.abilityScrolls().isEmpty()) {
			return false;
		}

		return modifiers.abilityScrolls().get().contains("WITHER_SHIELD_SCROLL");
	}

	private boolean onlyShowTimer() {
		return ConfigManager.getConfig().combat.witherShield.onlyShowTimer;
	}
}
