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
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
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
				Text.literal("§5Wither Shield: §e3.4s"),
				this::getText,
				ConfigManager.getConfig().combat.witherShield.hud,
				50,
				100
		);
	}

	@EventHandler(event = "UseItemCallback.EVENT")
	private ActionResult onUseItem(PlayerEntity player, World _world, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		if (isEnabled() && !stack.isEmpty() && stack.isOf(Items.IRON_SWORD) && hasWitherShieldScroll(stack)) {
			long now = System.currentTimeMillis();
			if (now >= cooldownEnd) {
				abilityEnd = now + ABSORPTION_COOLDOWN;
				cooldownEnd = now + ABSORPTION_COOLDOWN;
				readyUntil = -1L;
			}
		}
		return ActionResult.PASS;
	}

	private Text getText() {
		if (abilityEnd == -1L && cooldownEnd == -1L && readyUntil == -1L) {
			return Text.empty();
		}

		long now = System.currentTimeMillis();
		// Si ability est active
		if (abilityEnd > now) {
			double timeRemaining = (abilityEnd - now) / 1000.0d;
			return Text.empty()
					.append(Text.literal("Wither Shield: ").formatted(Formatting.DARK_PURPLE))
					.append(Text.literal(DECIMAL_FORMAT.format(timeRemaining) + "s").formatted(Formatting.YELLOW));
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
			return Text.empty()
					.append(Text.literal("Wither Shield: ").formatted(Formatting.DARK_PURPLE))
					.append(Text.literal("READY").formatted(Formatting.GREEN));
		}

		return Text.empty();
	}

	private boolean hasWitherShieldScroll(@NotNull ItemStack stack) {
		NbtCompound customData = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT).copyNbt();
		if (customData.isEmpty()) {
			return false;
		}

		Modifiers modifiers = Modifiers.ofNbt(customData);
		if (modifiers.abilityScrolls().isEmpty()) {
			return false;
		}

		return modifiers.abilityScrolls().get().contains("WITHER_SHIELD_SCROLL");
	}
}
