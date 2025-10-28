package fr.siroz.cariboustonks.feature.combat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.hud.Hud;
import fr.siroz.cariboustonks.manager.hud.HudProvider;
import fr.siroz.cariboustonks.manager.hud.TextHud;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.InventoryUtils;
import fr.siroz.cariboustonks.util.ItemUtils;
import it.unimi.dsi.fastutil.Pair;
import java.text.DecimalFormat;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class RagnarockAxeFeature extends Feature implements HudProvider {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_rag_axe");

	private static final String RAGNAROCK_AXE_ITEM_ID = "RAGNAROCK_AXE";
	private static final int RAGNAROCK_AXE_CAST_TIME = 10;
	private static final Pattern STRENGTH_PATTERN = Pattern.compile("Strength: \\+(?<strength>[\\d,.]+) *");
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

	private final Supplier<String> castMessageConfig = () -> ConfigManager.getConfig().combat.ragAxe.message;

	private Double lastStrength = null;
	private long lastCastTime = 0;

	public RagnarockAxeFeature() {
		NetworkEvents.PLAY_SOUND_PACKET.register(this::onPlaySound);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().combat.ragAxe.enabled;
	}

	@Override
	public @NotNull Pair<Identifier, Identifier> getAttachLayerAfter() {
		return Pair.of(VanillaHudElements.STATUS_EFFECTS, HUD_ID);
	}

	@Override
	public @NotNull Hud getHud() {
		return new TextHud(
				Text.literal("§cRag Casted! §e8.4s §f(§c+765.1§f)"),
				this::getText,
				ConfigManager.getConfig().combat.ragAxe.hud,
				200,
				10
		);
	}

	@Contract(pure = true)
	private @NotNull Text getText() {
		if (lastStrength == null || lastCastTime == 0) {
			return Text.empty();
		}

		long currentTime = System.currentTimeMillis();
		double timeRemaining = (lastCastTime - currentTime) / 1000.0;
		if (timeRemaining > 0) {
			String strength = lastStrength == 0 ? "?" : DECIMAL_FORMAT.format(lastStrength);
			return Text.empty()
					.append(Text.literal(castMessageConfig.get()))
					.append(Text.literal(" " + DECIMAL_FORMAT.format(timeRemaining) + "s").formatted(Formatting.YELLOW))
					.append(Text.literal(" (").formatted(Formatting.GRAY))
					.append(Text.literal("+" + strength).formatted(Formatting.RED))
					.append(Text.literal(")").formatted(Formatting.GRAY));
		} else {
			reset();
		}

		return Text.empty();
	}

	@EventHandler(event = "NetworkEvents.PLAY_SOUND_PACKET")
	private void onPlaySound(PlaySoundS2CPacket packet) {
		if (!isEnabled()) return;
		// 1.4920635f
		if (packet.getPitch() != 1.4920635f) return;
		if (packet.getSound() == null || packet.getSound().value() == null) return;
		// SoundEvents.WOLF_SOUNDS Map > sound path compare
		if (!packet.getSound().value().id().getPath().startsWith("entity.wolf.death")) return;

		ItemStack held = InventoryUtils.getHeldItem();
		if (held == null || held.isEmpty()) {
			return;
		}

		if (SkyBlockAPI.getSkyBlockItemId(held).equals(RAGNAROCK_AXE_ITEM_ID)) {
			double strength = getStrength(held);
			// Après avoir récupéré la Strength sur l'axe,
			// il faut s'assurer que celle-ci est bien un nombre valide,
			// puis multiplié par x1.5 pour respecter le Cast Ability.
			lastStrength = strength > 0 ? (strength * 1.5) : 0;
			lastCastTime = System.currentTimeMillis() + (RAGNAROCK_AXE_CAST_TIME * 1000L);
			// Si jamais la Strength est mal détecté, le Title sera quand même là.
			Client.showTitle(Text.literal(castMessageConfig.get()), 1, 20, 1);
		}
	}

	@Override
	protected void onClientJoinServer() {
		reset();
	}

	private void reset() {
		lastStrength = null;
		lastCastTime = 0;
	}

	private double getStrength(ItemStack item) {
		try {
			for (Text line : ItemUtils.getLore(item)) {
				Matcher strengthMatcher = STRENGTH_PATTERN.matcher(line.getString());
				if (strengthMatcher.find()) {
					return Double.parseDouble(strengthMatcher.group("strength"));
				}
			}
			return -1;
		} catch (Exception ignored) {
			return -1;
		}
	}
}
