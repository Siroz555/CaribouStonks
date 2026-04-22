package fr.siroz.cariboustonks.features.hunting.tracking;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigValue;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.MultiElementHud;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.core.module.hud.element.HudElement;
import fr.siroz.cariboustonks.core.skyblock.AttributeAPI;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarItemAnalytics;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarPriceType;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.Rarity;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockAttribute;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.TimeUtils;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class TrackingShardsFeature extends Feature {
	// Matches: "You caught [x<N> ]<ShardType> Shards[!]"
	// Group 1 → optional quantité (absent = 1)
	// Group 2 → shard type name
	private static final Pattern SHARD_CAUGHT_PATTERN = Pattern.compile("You caught (?:x(\\d+) )?(.+?) Shards?!");
	private static final long DELTA_DISPLAY_MS = 2_000;

	private final Map<String, Optional<SkyBlockAttribute>> attributeCache = new HashMap<>();
	private final HudElementBuilder hud = new HudElementBuilder();
	private final ConfigValue<Long> inactivityResetConfig = ConfigValue.of(
			() -> this.config().hunting.trackingShards.inactivityResetMs
	);
	@Nullable
	private ShardSessionStats cachedStats = null;
	private final ShardSession session;

	private int lastShardDelta = 0;
	private double lastCoinDelta = 0.0;
	private long deltaTimestamp = 0L;
	private boolean infoConfigNotified = false;

	public TrackingShardsFeature() {
		this.session = new ShardSession(
				this::resolveShardValue,
				this.inactivityResetConfig::get,
				() -> this.config().hunting.trackingShards.minPreWarmCatch
		);

		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onChatMessage);

		this.addComponent(HudComponent.class, HudComponent.builder()
				.attachAfterStatusEffects(CaribouStonks.identifier("shards_hud"))
				.hud(new MultiElementHud(
						() -> this.isEnabled() && this.session.isRunning(),
						new HudElementTextBuilder()
								.append(Component.literal("§6§l⚔ Shards Tracker"))
								.appendSpace()
								.append(Component.literal("§7Session: §e16h 16min"))
								.append(Component.literal("§7Total Shards: §a23081"))
								.append(Component.literal("§7Total Coins: §561,3M"))
								.append(Component.literal("§7Shards/h: §a1437"))
								.append(Component.literal("§7Coins/h: §635.8M"))
								.build(),
						this::getHudLines,
						this.config().hunting.trackingShards.hud,
						100,
						50
				))
				.build());
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().hunting.trackingShards.hud.enabled;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onChatMessage(@NonNull Component component) {
		if (!isEnabled()) return;

		// Il faut strip, ty Hypixel -_-
		String message = StonksUtils.stripColor(component.getString());
		Matcher shardCaughtMatcher = SHARD_CAUGHT_PATTERN.matcher(message);
		if (!shardCaughtMatcher.find()) return;

		try {
			int quantity = StonksUtils.toInt(shardCaughtMatcher.group(1), 1);
			String shardType = shardCaughtMatcher.group(2).trim();
			if (!shardType.isEmpty()) {
				handleCatch(quantity, shardType);
			}
		} catch (Exception ex) { // Je n'ai jamais confiance au Matcher
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.error("[{}] Unable to parse shard ({})", getShortName(), message, ex);
			}
		}
	}

	private void handleCatch(int quantity, @NonNull String shardType) {
		// Gestion du delta
		final int prevShards = cachedStats != null ? cachedStats.totalShards() : 0;
		final double prevCoins = cachedStats != null ? cachedStats.totalCoins() : 0;

		// Compute | recordCatch() computes tout
		cachedStats = session.recordCatch(shardType, quantity).orElse(null);

		// Gestion du delta
		if (cachedStats != null) {
			lastShardDelta = cachedStats.totalShards() - prevShards;
			lastCoinDelta = cachedStats.totalCoins() - prevCoins;
			deltaTimestamp = System.currentTimeMillis();
		}
	}

	private List<? extends HudElement> getHudLines() {
		// Trigger le reset de l'inactivité pendant le rendu, clear le cache si nécessaire
		if (session.tickInactivityCheck()) {
			cachedStats = null;
		}

		hud.clear();
		hud.appendLine(Component.literal("⚔ Shards Tracker").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
		hud.appendSpace();

		if (cachedStats == null) {
			// Pour informer le joueur
			warnInfoConfig();

			if (session.getState() == ShardSession.State.WARMING_UP) {
				int remaining = ShardSession.MIN_CATCHES_FOR_STATS - session.getCatchCount();
				hud.appendLine(Component.literal("Collecting... (" + remaining + " more needed)").withStyle(ChatFormatting.DARK_GRAY));
			}
		} else {
			renderActiveSession(cachedStats);
		}

		renderInactivityWarning();

		return hud.build();
	}

	private void renderActiveSession(@NonNull ShardSessionStats stats) {
		boolean showDelta = System.currentTimeMillis() - deltaTimestamp < DELTA_DISPLAY_MS;

		// 1 type breakdown visible uniquement quand il y a uniquement une type de shards
		if (stats.shardsByType().size() == 1 && stats.totalShards() > 1) {
			String type = stats.shardsByType().keySet().iterator().next();
			hud.appendLine(Component.literal(type).withStyle(resolveShardRarity(type).getFormatting()));
			hud.appendSpace();
		}

		hud.appendLine(Component.literal("Session: ").withStyle(ChatFormatting.GRAY).append(
				Component.literal(TimeUtils.getDurationFormatted(Instant.ofEpochMilli(session.getSessionStartMs()), Instant.now(), false)).withStyle(ChatFormatting.YELLOW))
		);
		hud.appendSpace();

		MutableComponent shardsLine = Component.literal("Total Shards: ").withStyle(ChatFormatting.GRAY).append(
				Component.literal(StonksUtils.INTEGER_NUMBERS.format(stats.totalShards())).withStyle(ChatFormatting.GREEN));
		if (showDelta && lastShardDelta > 0) {
			shardsLine.append(Component.literal(" (+" + lastShardDelta + ")").withStyle(ChatFormatting.DARK_GREEN));
		}

		MutableComponent coinsLine = Component.literal("Total Coins: ").withStyle(ChatFormatting.GRAY).append(
				Component.literal(StonksUtils.INTEGER_NUMBERS.format(stats.totalCoins())).withStyle(ChatFormatting.GOLD));
		if (showDelta && lastCoinDelta > 0) {
			coinsLine.append(Component.literal(" (+" + StonksUtils.SHORT_FLOAT_NUMBERS.format(lastCoinDelta) + ")").withStyle(ChatFormatting.YELLOW));
		}

		hud.appendLine(shardsLine);
		hud.appendLine(coinsLine);
		hud.appendSpace();

		hud.appendLine(Component.literal("Shards/h: ").withStyle(ChatFormatting.GRAY).append(
				Component.literal(StonksUtils.INTEGER_NUMBERS.format(stats.shardsPerHour())).withStyle(ChatFormatting.GREEN))
		);
		hud.appendLine(Component.literal("Coins/h: ").withStyle(ChatFormatting.GRAY).append(
				Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(stats.coinsPerHour())).withStyle(ChatFormatting.GOLD))
		);

		// Par type breakdown visible uniquement quand il y a plusieurs type de shards
		if (stats.shardsByType().size() > 1) {
			hud.appendSpace();
			hud.appendLine(Component.literal("By type:").withStyle(ChatFormatting.DARK_GRAY));
			hud.appendSpace();
			for (Map.Entry<String, Integer> entry : stats.shardsByType().entrySet()) {
				hud.appendLine(Component.literal(" " + entry.getKey() + ": ").withStyle(resolveShardRarity(entry.getKey()).getFormatting())
						.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(entry.getValue())).withStyle(ChatFormatting.YELLOW))
				);
			}
		}
	}

	private void renderInactivityWarning() {
		long timeSinceCatch = System.currentTimeMillis() - session.getLastCatchTime();
		long timeout = inactivityResetConfig.get();
		long halfTimeout = timeout / 2;
		if (timeSinceCatch < halfTimeout) return;

		long remaining = (timeout - timeSinceCatch) / 1_000; // en secondes
		if (remaining < 0) return;

		hud.appendSpace();
		hud.appendLine(Component.literal("Reset in ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
				.append(Component.literal(remaining + "s").withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
		);
	}

	private Optional<SkyBlockAttribute> resolveAttribute(String shardType) {
		return attributeCache.computeIfAbsent(shardType,
				k -> Optional.ofNullable(AttributeAPI.getAttributeByName(k)));
	}

	private Double resolveShardValue(String shardType) {
		// SIROZ-NOTE: check si ironman ou non aussi
		boolean useBuyPrice = this.config().hunting.trackingShards.priceType == BazaarPriceType.BUY;

		return resolveAttribute(shardType)
				.map(attribute -> CaribouStonks.skyBlock()
						.getHypixelDataSource()
						.getBazaarItem(attribute.skyBlockApiId())
						.map(useBuyPrice ? BazaarItemAnalytics.BUY : BazaarItemAnalytics.SELL)
						.orElse(0.0))
				.orElse(0.0);
	}

	private Rarity resolveShardRarity(String shardType) {
		return resolveAttribute(shardType)
				.map(SkyBlockAttribute::getRarityFromId)
				.orElse(Rarity.UNKNOWN);
	}

	private void warnInfoConfig() {
		if (infoConfigNotified) return;
		infoConfigNotified = true;

		Client.sendMessage(Component.empty());
		Client.sendMessageWithPrefix(Component.literal("⚔ Shards Tracker").withStyle(ChatFormatting.GOLD)
				.append(Component.literal(" is currently running.").withStyle(ChatFormatting.YELLOW)));
		Client.sendMessage(Component.literal(" Disable this in Skills > Hunting > Shards Tracker").withStyle(ChatFormatting.DARK_GRAY));
		Client.sendMessage(Component.empty());
	}
}
