package fr.siroz.cariboustonks.feature.hunting.tracking;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.config.ConfigManager;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.BazaarItemAnalytics;
import fr.siroz.cariboustonks.core.data.hypixel.bazaar.BazaarPriceType;
import fr.siroz.cariboustonks.core.data.hypixel.item.Rarity;
import fr.siroz.cariboustonks.core.data.mod.SkyBlockAttribute;
import fr.siroz.cariboustonks.core.skyblock.AttributeAPI;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.ChatEvents;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.feature.Feature;
import fr.siroz.cariboustonks.manager.hud.Hud;
import fr.siroz.cariboustonks.manager.hud.HudProvider;
import fr.siroz.cariboustonks.manager.hud.MultiElementHud;
import fr.siroz.cariboustonks.manager.hud.builder.HudElementBuilder;
import fr.siroz.cariboustonks.manager.hud.builder.HudElementTextBuilder;
import fr.siroz.cariboustonks.manager.hud.element.HudElement;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.StonksUtils;
import fr.siroz.cariboustonks.util.TimeUtils;
import it.unimi.dsi.fastutil.Pair;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TrackingShardsFeature extends Feature implements HudProvider {
	// Matches: "You caught [x<N> ]<ShardType> Shards[!]"
	// Group 1 → optional quantité (absent = 1)
	// Group 2 → shard type name
	private static final Pattern SHARD_CAUGHT_PATTERN = Pattern.compile("You caught (?:x(\\d+) )?(.+?) Shards?!");
	private static final Identifier HUD_ID = CaribouStonks.identifier("shards_hud");
	private static final long DELTA_DISPLAY_MS = 2_000;

	private final Map<String, Optional<SkyBlockAttribute>> attributeCache = new HashMap<>();
	private final HudElementBuilder hud = new HudElementBuilder();
	private final Supplier<Long> inactivityResetConfig = () -> ConfigManager.getConfig().hunting.trackingShards.inactivityResetMs;
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
				this.inactivityResetConfig,
				() -> ConfigManager.getConfig().hunting.trackingShards.minPreWarmCatch
		);

		ChatEvents.MESSAGE_RECEIVED.register(this::onChatMessage);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && ConfigManager.getConfig().hunting.trackingShards.hud.enabled;
	}

	@Override
	public @NotNull Pair<Identifier, Identifier> getAttachLayerAfter() {
		return Pair.of(VanillaHudElements.STATUS_EFFECTS, HUD_ID);
	}

	@Override
	public @NotNull Hud getHud() {
		return new MultiElementHud(
				() -> this.isEnabled() && this.session.isRunning(),
				new HudElementTextBuilder()
						.append(Text.literal("§6§l⚔ Shards Tracker"))
						.appendSpace()
						.append(Text.literal("§7Session: §e16h 16min"))
						.append(Text.literal("§7Total Shards: §a23081"))
						.append(Text.literal("§7Total Coins: §561,3M"))
						.append(Text.literal("§7Shards/h: §a1437"))
						.append(Text.literal("§7Coins/h: §635.8M"))
						.build(),
				this::getHudLines,
				ConfigManager.getConfig().hunting.trackingShards.hud,
				100,
				50
		);
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onChatMessage(Text component) {
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

	private void handleCatch(int quantity, String shardType) {
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
		hud.appendLine(Text.literal("⚔ Shards Tracker").formatted(Formatting.GOLD, Formatting.BOLD));
		hud.appendSpace();

		if (cachedStats == null) {
			// Pour informer le joueur
			warnInfoConfig();

			if (session.getState() == ShardSession.State.WARMING_UP) {
				int remaining = ShardSession.MIN_CATCHES_FOR_STATS - session.getCatchCount();
				hud.appendLine(Text.literal("Collecting... (" + remaining + " more needed)").formatted(Formatting.DARK_GRAY));
			}
		} else {
			renderActiveSession(cachedStats);
		}

		renderInactivityWarning();

		return hud.build();
	}

	private void renderActiveSession(ShardSessionStats stats) {
		boolean showDelta = System.currentTimeMillis() - deltaTimestamp < DELTA_DISPLAY_MS;

		// 1 type breakdown visible uniquement quand il y a uniquement une type de shards
		if (stats.shardsByType().size() == 1 && stats.totalShards() > 1) {
			String type = stats.shardsByType().keySet().iterator().next();
			hud.appendLine(Text.literal(type).formatted(resolveShardRarity(type).getFormatting()));
			hud.appendSpace();
		}

		hud.appendLine(Text.literal("Session: ").formatted(Formatting.GRAY).append(
				Text.literal(TimeUtils.getDurationFormatted(Instant.ofEpochMilli(session.getSessionStartMs()), Instant.now(), false)).formatted(Formatting.YELLOW))
		);
		hud.appendSpace();

		MutableText shardsLine = Text.literal("Total Shards: ").formatted(Formatting.GRAY).append(
				Text.literal(StonksUtils.INTEGER_NUMBERS.format(stats.totalShards())).formatted(Formatting.GREEN));
		if (showDelta && lastShardDelta > 0) {
			shardsLine.append(Text.literal(" (+" + lastShardDelta + ")").formatted(Formatting.DARK_GREEN));
		}

		MutableText coinsLine = Text.literal("Total Coins: ").formatted(Formatting.GRAY).append(
				Text.literal(StonksUtils.INTEGER_NUMBERS.format(stats.totalCoins())).formatted(Formatting.GOLD));
		if (showDelta && lastCoinDelta > 0) {
			coinsLine.append(Text.literal(" (+" + StonksUtils.SHORT_FLOAT_NUMBERS.format(lastCoinDelta) + ")").formatted(Formatting.YELLOW));
		}

		hud.appendLine(shardsLine);
		hud.appendLine(coinsLine);
		hud.appendSpace();

		hud.appendLine(Text.literal("Shards/h: ").formatted(Formatting.GRAY).append(
				Text.literal(StonksUtils.INTEGER_NUMBERS.format(stats.shardsPerHour())).formatted(Formatting.GREEN))
		);
		hud.appendLine(Text.literal("Coins/h: ").formatted(Formatting.GRAY).append(
				Text.literal(StonksUtils.SHORT_FLOAT_NUMBERS.format(stats.coinsPerHour())).formatted(Formatting.GOLD))
		);

		// Par type breakdown visible uniquement quand il y a plusieurs type de shards
		if (stats.shardsByType().size() > 1) {
			hud.appendSpace();
			hud.appendLine(Text.literal("By type:").formatted(Formatting.DARK_GRAY));
			hud.appendSpace();
			for (Map.Entry<String, Integer> entry : stats.shardsByType().entrySet()) {
				hud.appendLine(Text.literal(" " + entry.getKey() + ": ").formatted(resolveShardRarity(entry.getKey()).getFormatting())
						.append(Text.literal(StonksUtils.INTEGER_NUMBERS.format(entry.getValue())).formatted(Formatting.YELLOW))
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
		hud.appendLine(Text.literal("Reset in ").formatted(Formatting.RED, Formatting.BOLD)
				.append(Text.literal(remaining + "s").formatted(Formatting.YELLOW, Formatting.BOLD))
		);
	}

	private Optional<SkyBlockAttribute> resolveAttribute(String shardType) {
		return attributeCache.computeIfAbsent(shardType,
				k -> Optional.ofNullable(AttributeAPI.getAttributeByName(k)));
	}

	private Double resolveShardValue(String shardType) {
		// SIROZ-NOTE: check si ironman ou non aussi
		boolean useBuyPrice = ConfigManager.getConfig().hunting.trackingShards.priceType == BazaarPriceType.BUY;

		return resolveAttribute(shardType)
				.map(attribute -> CaribouStonks.core()
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

		Client.sendMessage(Text.empty());
		Client.sendMessageWithPrefix(Text.literal("⚔ Shards Tracker").formatted(Formatting.GOLD)
				.append(Text.literal(" is currently running.").formatted(Formatting.YELLOW)));
		Client.sendMessage(Text.literal(" Disable this in Skills > Hunting > Shards Tracker").formatted(Formatting.DARK_GRAY));
		Client.sendMessage(Text.empty());
	}
}
