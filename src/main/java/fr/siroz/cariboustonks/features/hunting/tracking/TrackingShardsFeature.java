package fr.siroz.cariboustonks.features.hunting.tracking;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigValue;
import fr.siroz.cariboustonks.core.component.HudComponent;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.module.hud.MultiElementHud;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.core.module.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.core.skyblock.AttributeAPI;
import fr.siroz.cariboustonks.core.skyblock.IslandType;
import fr.siroz.cariboustonks.core.skyblock.Rarity;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarItemAnalytics;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.bazaar.BazaarPriceType;
import fr.siroz.cariboustonks.core.skyblock.item.SkyBlockAttribute;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.TimeUtils;
import java.time.Instant;
import java.util.HashMap;
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
	private static final Pattern LOOT_SHARE_PATTERN = Pattern.compile("LOOT SHARE You received (?:(\\d+) )?(.+?) Shards for assisting \\w+!");
	private static final long DELTA_DISPLAY_MS = 2_000;

	private final Map<String, Optional<SkyBlockAttribute>> attributeCache = new HashMap<>();
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
		return SkyBlockAPI.isOnSkyBlock()
				&& SkyBlockAPI.getIsland() != IslandType.DUNGEON
				&& this.config().hunting.trackingShards.hud.enabled;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onChatMessage(@NonNull Component component) {
		if (!isEnabled()) return;

		// Il faut strip, ty Hypixel -_-
		String message = StonksUtils.stripColor(component.getString());

		Matcher shardCaughtMatcher = SHARD_CAUGHT_PATTERN.matcher(message);
		if (shardCaughtMatcher.find()) {
			handleCatchParsing(shardCaughtMatcher, message, false);
			return;
		}

		Matcher lootShareMatcher = LOOT_SHARE_PATTERN.matcher(message);
		if (lootShareMatcher.find() && this.config().hunting.trackingShards.includeLootShare) {
			handleCatchParsing(lootShareMatcher, message, true);
		}
	}

	private void handleCatchParsing(Matcher matcher, String debugMessage, boolean isLootShare) {
		try {
			int quantity = StonksUtils.toInt(matcher.group(1), 1);
			String shardType = matcher.group(2).trim();
			if (!shardType.isEmpty()) {
				handleCatch(quantity, shardType, isLootShare);
			}
		} catch (Exception ex) { // Je n'ai jamais confiance au Matcher
			if (DeveloperTools.isInDevelopment()) {
				CaribouStonks.LOGGER.error("[{}] Unable to parse LS shard ({})", getShortName(), debugMessage, ex);
			}
		}
	}

	private void handleCatch(int quantity, @NonNull String shardType, boolean isLootShare) {
		// Gestion du delta
		final int prevShards = cachedStats != null ? cachedStats.totalShards() : 0;
		final double prevCoins = cachedStats != null ? cachedStats.totalCoins() : 0;

		// Compute | recordCatch() computes tout
		cachedStats = session.recordCatch(shardType, quantity, isLootShare).orElse(null);

		// Gestion du delta
		if (cachedStats != null) {
			lastShardDelta = cachedStats.totalShards() - prevShards;
			lastCoinDelta = cachedStats.totalCoins() - prevCoins;
			deltaTimestamp = System.currentTimeMillis();
		}
	}

	private void getHudLines(HudElementBuilder builder) {
		// Trigger le reset de l'inactivité pendant le rendu, clear le cache si nécessaire
		if (session.tickInactivityCheck()) {
			cachedStats = null;
		}

		builder.appendLine(Component.literal("⚔ Shards Tracker").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
		builder.appendSpace();

		if (cachedStats == null) {
			// Pour informer le joueur
			warnInfoConfig();

			if (session.getState() == ShardSession.State.WARMING_UP) {
				int remaining = ShardSession.MIN_CATCHES_FOR_STATS - session.getCatchCount();
				builder.appendLine(Component.literal("Collecting... (" + remaining + " more needed)").withStyle(ChatFormatting.DARK_GRAY));
			}
		} else {
			renderActiveSession(builder, cachedStats);
		}

		renderInactivityWarning(builder);
	}

	private void renderActiveSession(HudElementBuilder builder, @NonNull ShardSessionStats stats) {
		boolean showDelta = System.currentTimeMillis() - deltaTimestamp < DELTA_DISPLAY_MS;

		// 1 type breakdown visible uniquement quand il y a uniquement une type de shards
		if (stats.shardsByType().size() == 1 && stats.totalShards() > 1) {
			String type = stats.shardsByType().keySet().iterator().next();
			builder.appendLine(Component.literal(type).withColor(resolveShardRarity(type).getColor()));
			builder.appendSpace();
		}

		builder.appendLine(Component.literal("Session: ").withStyle(ChatFormatting.GRAY).append(
				Component.literal(TimeUtils.getDurationFormatted(Instant.ofEpochMilli(session.getSessionStartMs()), Instant.now(), false)).withStyle(ChatFormatting.YELLOW))
		);
		builder.appendSpace();

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

		builder.appendLine(shardsLine);
		builder.appendLine(coinsLine);
		builder.appendSpace();

		builder.appendLine(Component.literal("Shards/h: ").withStyle(ChatFormatting.GRAY).append(
				Component.literal(StonksUtils.INTEGER_NUMBERS.format(stats.shardsPerHour())).withStyle(ChatFormatting.GREEN))
		);
		builder.appendLine(Component.literal("Coins/h: ").withStyle(ChatFormatting.GRAY).append(
				Component.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(stats.coinsPerHour())).withStyle(ChatFormatting.GOLD))
		);

		if (stats.lootShareCount() > 0) {
			builder.appendSpace();
			builder.appendLine(Component.literal("Loot Share: ").withStyle(ChatFormatting.YELLOW).append(
					Component.literal(StonksUtils.INTEGER_NUMBERS.format(stats.lootShareCount())).withStyle(ChatFormatting.AQUA))
			);
		}

		// Par type breakdown visible uniquement quand il y a plusieurs type de shards
		if (stats.shardsByType().size() > 1) {
			builder.appendSpace();
			builder.appendLine(Component.literal("By type:").withStyle(ChatFormatting.DARK_GRAY));
			builder.appendSpace();
			for (Map.Entry<String, Integer> entry : stats.shardsByType().entrySet()) {
				builder.appendLine(Component.literal(" " + entry.getKey() + ": ").withColor(resolveShardRarity(entry.getKey()).getColor())
						.append(Component.literal(StonksUtils.INTEGER_NUMBERS.format(entry.getValue())).withStyle(ChatFormatting.YELLOW))
				);
			}
		}
	}

	private void renderInactivityWarning(HudElementBuilder builder) {
		long timeSinceCatch = System.currentTimeMillis() - session.getLastCatchTime();
		long timeout = inactivityResetConfig.get();
		long halfTimeout = timeout / 2;
		if (timeSinceCatch < halfTimeout) return;

		long remaining = (timeout - timeSinceCatch) / 1_000; // en secondes
		if (remaining < 0) return;

		builder.appendSpace();
		builder.appendLine(Component.literal("Reset in ").withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
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

		PlayerContext.sendMessage(Component.empty());
		PlayerContext.sendMessageWithPrefix(Component.literal("⚔ Shards Tracker").withStyle(ChatFormatting.GOLD)
				.append(Component.literal(" is currently running.").withStyle(ChatFormatting.YELLOW)));
		PlayerContext.sendMessage(Component.literal(" Disable this in Skills > Hunting > Shards Tracker").withStyle(ChatFormatting.DARK_GRAY));
		PlayerContext.sendMessage(Component.empty());
	}
}
