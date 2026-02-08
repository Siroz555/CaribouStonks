package fr.siroz.cariboustonks.features.combat;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigValue;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.TextHud;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.ItemUtils;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public class RagnarockAxeFeature extends Feature {

	private static final Identifier HUD_ID = CaribouStonks.identifier("hud_rag_axe");
	private static final String RAGNAROCK_AXE_ITEM_ID = "RAGNAROCK_AXE";
	private static final int RAGNAROCK_AXE_CAST_TIME = 10;
	private static final Pattern STRENGTH_PATTERN = Pattern.compile("Strength: \\+(?<strength>[\\d,.]+) *");
	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.0");

	private final ConfigValue<String> configCastMessage = ConfigValue.of(
			() -> this.config().combat.ragAxe.message
	);

	private Double lastStrength = null;
	private long lastCastTime = 0;

	public RagnarockAxeFeature() {
		NetworkEvents.PLAY_SOUND_PACKET.register(this::onPlaySound);

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfter(VanillaHudElements.STATUS_EFFECTS, HUD_ID)
				.hud(new TextHud(
						Component.literal("§cRag Casted! §e8.4s §f(§c+765.1§f)"),
						this::getText,
						this.config().combat.ragAxe.hud,
						200,
						10
				))
				.build()
		);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().combat.ragAxe.enabled;
	}

	@EventHandler(event = "NetworkEvents.PLAY_SOUND_PACKET")
	private void onPlaySound(ClientboundSoundPacket packet) {
		if (!isEnabled()) return;
		if (packet.getPitch() != 1.4920635f) return;
		if (!Client.convertSoundPacketToName(packet).startsWith("entity.wolf.death")) return;

		ItemStack held = Client.getHeldItem();
		if (held == null || held.isEmpty()) return;

		if (SkyBlockAPI.getSkyBlockItemId(held).equals(RAGNAROCK_AXE_ITEM_ID)) {
			double strength = getStrength(held);
			// Après avoir récupéré la Strength sur l'axe,
			// il faut s'assurer que celle-ci est bien un nombre valide,
			// puis multiplié par x1.5 pour respecter le Cast Ability.
			lastStrength = strength > 0 ? (strength * 1.5) : 0;
			lastCastTime = System.currentTimeMillis() + (RAGNAROCK_AXE_CAST_TIME * 1000L);
			// Si jamais la Strength est mal détecté, le Title sera quand même là.
			Client.showTitle(Component.literal(configCastMessage.get()), 1, 20, 1);
		}
	}

	@Override
	protected void onClientJoinServer() {
		reset();
	}

	@NonNull
	private Component getText() {
		if (lastStrength == null || lastCastTime == 0) return Component.empty();

		long currentTime = System.currentTimeMillis();
		double timeRemaining = (lastCastTime - currentTime) / 1000.0;
		if (timeRemaining > 0) {
			String strength = lastStrength == 0 ? "?" : DECIMAL_FORMAT.format(lastStrength);
			return Component.empty()
					.append(Component.literal(configCastMessage.get()))
					.append(Component.literal(" " + DECIMAL_FORMAT.format(timeRemaining) + "s").withStyle(ChatFormatting.YELLOW))
					.append(Component.literal(" (").withStyle(ChatFormatting.GRAY))
					.append(Component.literal("+" + strength).withStyle(ChatFormatting.RED))
					.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
		} else {
			reset();
		}

		return Component.empty();
	}

	private void reset() {
		lastStrength = null;
		lastCastTime = 0;
	}

	private double getStrength(ItemStack item) {
		try {
			for (Component line : ItemUtils.getLore(item)) {
				Matcher strengthMatcher = STRENGTH_PATTERN.matcher(line.getString());
				if (strengthMatcher.find()) {
					return Double.parseDouble(strengthMatcher.group("strength"));
				}
			}
			return -1;
		} catch (Exception ex) {
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.warn("{} Unable to parse Strength from ItemStack", getShortName(), ex);
			}
			return -1;
		}
	}
}
