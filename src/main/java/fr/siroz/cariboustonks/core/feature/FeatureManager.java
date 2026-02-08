package fr.siroz.cariboustonks.core.feature;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.annotation.Experimental;
import fr.siroz.cariboustonks.features.chat.ChatColorationFeature;
import fr.siroz.cariboustonks.features.chat.ChatPositionFeature;
import fr.siroz.cariboustonks.features.chat.CopyChatMessageFeature;
import fr.siroz.cariboustonks.features.combat.CocoonedWarningFeature;
import fr.siroz.cariboustonks.features.combat.LowHealthWarningFeature;
import fr.siroz.cariboustonks.features.combat.RagnarockAxeFeature;
import fr.siroz.cariboustonks.features.combat.SecondLifeFeature;
import fr.siroz.cariboustonks.features.combat.WitherShieldFeature;
import fr.siroz.cariboustonks.features.dungeon.CroesusMenuFeature;
import fr.siroz.cariboustonks.features.dungeon.SadanBossFeature;
import fr.siroz.cariboustonks.features.dungeon.ThornBossFeature;
import fr.siroz.cariboustonks.features.dungeon.WitherKingDragonFeature;
import fr.siroz.cariboustonks.features.fishing.BobberTimerFeature;
import fr.siroz.cariboustonks.features.fishing.FishCaughtFeature;
import fr.siroz.cariboustonks.features.fishing.HotspotFeature;
import fr.siroz.cariboustonks.features.fishing.HotspotRadarFeature;
import fr.siroz.cariboustonks.features.fishing.RareSeaCreatureFeature;
import fr.siroz.cariboustonks.features.foraging.BreakTreeAnimationFeature;
import fr.siroz.cariboustonks.features.foraging.TreeOverlayFeature;
import fr.siroz.cariboustonks.features.garden.DisableWateringCanPlacementFeature;
import fr.siroz.cariboustonks.features.garden.GreenhouseGrowthStageFeature;
import fr.siroz.cariboustonks.features.garden.MouseLockFeature;
import fr.siroz.cariboustonks.features.garden.pest.PestFinderFeature;
import fr.siroz.cariboustonks.features.garden.pest.PlotInfestedFeature;
import fr.siroz.cariboustonks.features.hunting.AttributeInfoTooltipFeature;
import fr.siroz.cariboustonks.features.item.ColoredEnchantmentFeature;
import fr.siroz.cariboustonks.features.item.TooltipDecoratorFeature;
import fr.siroz.cariboustonks.features.keyshortcut.KeyShortcutFeature;
import fr.siroz.cariboustonks.features.misc.HexTooltipFeature;
import fr.siroz.cariboustonks.features.misc.HighlightMobFeature;
import fr.siroz.cariboustonks.features.misc.HoppityEggFinderFeature;
import fr.siroz.cariboustonks.features.misc.PartyCommandFeature;
import fr.siroz.cariboustonks.features.misc.REISearchBarCalculatorFeature;
import fr.siroz.cariboustonks.features.reminders.BoosterCookieReminderFeature;
import fr.siroz.cariboustonks.features.reminders.ChocolateLimitReminderFeature;
import fr.siroz.cariboustonks.features.reminders.EnchantedCloakReminderFeature;
import fr.siroz.cariboustonks.features.reminders.ForgeReminderFeature;
import fr.siroz.cariboustonks.features.reminders.StonksAuctionReminderFeature;
import fr.siroz.cariboustonks.features.reminders.UbikCubeReminderFeature;
import fr.siroz.cariboustonks.features.slayer.HighlightSlayerMobFeature;
import fr.siroz.cariboustonks.features.slayer.SlayerCocoonedWarningFeature;
import fr.siroz.cariboustonks.features.slayer.SlayerStatsFeature;
import fr.siroz.cariboustonks.features.slayer.boss.TarantulaBossFeature;
import fr.siroz.cariboustonks.features.stonks.ItemValueViewerFeature;
import fr.siroz.cariboustonks.features.stonks.StonksCommandFeature;
import fr.siroz.cariboustonks.features.stonks.StonksFeature;
import fr.siroz.cariboustonks.features.stonks.tooltips.ItemValueTooltipFeature;
import fr.siroz.cariboustonks.features.stonks.tooltips.auction.AuctionLowestBinTooltipFeature;
import fr.siroz.cariboustonks.features.stonks.tooltips.bazaar.BazaarPriceTooltipFeature;
import fr.siroz.cariboustonks.features.ui.AbiphoneFavoriteContactFeature;
import fr.siroz.cariboustonks.features.ui.SelectedPetHighlightFeature;
import fr.siroz.cariboustonks.features.ui.hud.DayHud;
import fr.siroz.cariboustonks.features.ui.hud.FpsHud;
import fr.siroz.cariboustonks.features.ui.hud.PingHud;
import fr.siroz.cariboustonks.features.ui.hud.TpsHud;
import fr.siroz.cariboustonks.features.ui.overlay.EtherWarpOverlayFeature;
import fr.siroz.cariboustonks.features.ui.overlay.GyrokineticOverlayFeature;
import fr.siroz.cariboustonks.features.ui.tracking.MobTrackingFeature;
import fr.siroz.cariboustonks.features.vanilla.HideStatusEffectsFeature;
import fr.siroz.cariboustonks.features.vanilla.MuteVanillaSoundFeature;
import fr.siroz.cariboustonks.features.vanilla.ScrollableTooltipFeature;
import fr.siroz.cariboustonks.features.vanilla.ZoomFeature;
import fr.siroz.cariboustonks.features.waypoints.WaypointFeature;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jspecify.annotations.NonNull;

public final class FeatureManager {

	private static final Map<Class<? extends Feature>, Feature> FEATURE_INSTANCES = new ConcurrentHashMap<>();

	public FeatureManager() {
		// Chat
		registerFeature(new ChatColorationFeature());
		registerFeature(new ChatPositionFeature());
		registerFeature(new CopyChatMessageFeature());
		// Combat
		registerFeature(new CocoonedWarningFeature());
		registerFeature(new LowHealthWarningFeature());
		registerFeature(new RagnarockAxeFeature());
		registerFeature(new SecondLifeFeature());
		registerFeature(new WitherShieldFeature());
		// Dungeons
		registerFeature(new CroesusMenuFeature());
		registerFeature(new ThornBossFeature());
		registerFeature(new SadanBossFeature());
		registerFeature(new WitherKingDragonFeature());
		// Fishing
		registerFeature(new RareSeaCreatureFeature());
		registerFeature(new BobberTimerFeature());
		registerFeature(new FishCaughtFeature());
		registerFeature(new HotspotFeature());
		registerFeature(new HotspotRadarFeature());
		// Foraging
		registerFeature(new BreakTreeAnimationFeature());
		registerFeature(new TreeOverlayFeature());
		// Garden
		registerFeature(new PestFinderFeature());
		registerFeature(new PlotInfestedFeature());
		registerFeature(new DisableWateringCanPlacementFeature());
		registerFeature(new GreenhouseGrowthStageFeature());
		registerFeature(new MouseLockFeature());
		// Hunting
		registerFeature(new AttributeInfoTooltipFeature(3));
		// Item
		registerFeature(new ColoredEnchantmentFeature());
		registerFeature(new TooltipDecoratorFeature());
		// KeyShortcut
		registerFeature(new KeyShortcutFeature());
		// Pet
		registerFeature(new SelectedPetHighlightFeature());
		// Reminders
		registerFeature(new BoosterCookieReminderFeature());
		registerFeature(new ChocolateLimitReminderFeature());
		registerFeature(new EnchantedCloakReminderFeature());
		registerFeature(new ForgeReminderFeature());
		registerFeature(new StonksAuctionReminderFeature());
		registerFeature(new UbikCubeReminderFeature());
		// Slayer
		registerFeature(new HighlightSlayerMobFeature());
		registerFeature(new SlayerCocoonedWarningFeature());
		registerFeature(new SlayerStatsFeature());
		// Slayer - Boss
		registerFeature(new TarantulaBossFeature());
		// Stonks
		registerFeature(new ItemValueViewerFeature());
		registerFeature(new StonksCommandFeature());
		registerFeature(new StonksFeature());
		registerFeature(new BazaarPriceTooltipFeature(1));
		registerFeature(new AuctionLowestBinTooltipFeature(2));
		registerFeature(new ItemValueTooltipFeature(4));
		// UI
		registerFeature(new MobTrackingFeature());
		registerFeature(new AbiphoneFavoriteContactFeature(0));
		// UI - HUDs
		registerFeature(new FpsHud());
		registerFeature(new PingHud());
		registerFeature(new TpsHud());
		registerFeature(new DayHud());
		// UI - Overlays
		registerFeature(new EtherWarpOverlayFeature());
		registerFeature(new GyrokineticOverlayFeature());
		// Waypoint
		registerFeature(new WaypointFeature());
		// Misc
		registerFeature(new HexTooltipFeature(5));
		registerFeature(new HighlightMobFeature());
		registerFeature(new HoppityEggFinderFeature());
		registerFeature(new PartyCommandFeature());
		registerFeature(new REISearchBarCalculatorFeature());
		// Vanilla
		registerFeature(new HideStatusEffectsFeature());
		registerFeature(new MuteVanillaSoundFeature());
		registerFeature(new ScrollableTooltipFeature());
		registerFeature(new ZoomFeature());

		// Après les enregistrements, initialise les dépendances
		postInitialize();
		// Enregistre les listeners
		registerListeners();
		CaribouStonks.LOGGER.info("{} features are now loaded and ready", FEATURE_INSTANCES.size());
	}

	@SuppressWarnings("unchecked")
	public <T extends Feature> T getFeature(@NonNull Class<T> featureClass) {
		return (T) FEATURE_INSTANCES.get(featureClass);
	}

	private void registerFeature(@NonNull Feature feature) {
		FEATURE_INSTANCES.put(feature.getClass(), feature);

		warnIfExperimental(feature);

		CaribouStonks.systems().handleFeatureRegistration(feature);
	}

	private void postInitialize() {
		for (Feature feature : FEATURE_INSTANCES.values()) {
			feature.postInitialize(this);
		}
	}

	private void registerListeners() {
		ClientTickEvents.END_CLIENT_TICK.register(_mc -> {
			for (Feature feature : FEATURE_INSTANCES.values()) {
				feature.onClientTick();
			}
		});
		ClientPlayConnectionEvents.JOIN.register((_h, _ps, _mc) -> {
			for (Feature feature : FEATURE_INSTANCES.values()) {
				feature.onClientJoinServer();
			}
		});
	}

	private void warnIfExperimental(Feature feature) {
		Experimental annotation = getClass().getAnnotation(Experimental.class);

		Optional<String> experimental = annotation != null ? Optional.of(annotation.value()) : Optional.empty();
		experimental.ifPresent(s -> CaribouStonks.LOGGER.warn("[FeatureManager] {} is marked as Experimental {}",
				feature.getClass().getSimpleName(), "(" + s + ")"));
	}
}
