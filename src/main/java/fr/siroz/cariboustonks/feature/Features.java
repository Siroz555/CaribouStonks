package fr.siroz.cariboustonks.feature;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.feature.chat.ChatColorationFeature;
import fr.siroz.cariboustonks.feature.chat.ChatPositionFeature;
import fr.siroz.cariboustonks.feature.chat.CopyChatMessageFeature;
import fr.siroz.cariboustonks.feature.combat.CocoonedWarningFeature;
import fr.siroz.cariboustonks.feature.combat.LowHealthWarningFeature;
import fr.siroz.cariboustonks.feature.combat.RagnarockAxeFeature;
import fr.siroz.cariboustonks.feature.combat.SecondLifeFeature;
import fr.siroz.cariboustonks.feature.dungeon.CroesusMenuFeature;
import fr.siroz.cariboustonks.feature.dungeon.NecronBossFeature;
import fr.siroz.cariboustonks.feature.dungeon.SadanBossFeature;
import fr.siroz.cariboustonks.feature.dungeon.ThornBossFeature;
import fr.siroz.cariboustonks.feature.dungeon.WitherKingDragonFeature;
import fr.siroz.cariboustonks.feature.fishing.BobberTimerFeature;
import fr.siroz.cariboustonks.feature.fishing.FishCaughtFeature;
import fr.siroz.cariboustonks.feature.fishing.RareSeaCreatureFeature;
import fr.siroz.cariboustonks.feature.fishing.hotspot.HotspotFeature;
import fr.siroz.cariboustonks.feature.fishing.radar.HotspotRadarFeature;
import fr.siroz.cariboustonks.feature.foraging.BreakTreeAnimationFeature;
import fr.siroz.cariboustonks.feature.foraging.TreeOverlayFeature;
import fr.siroz.cariboustonks.feature.garden.DisableWateringCanPlacementFeature;
import fr.siroz.cariboustonks.feature.garden.GreenhouseGrowthStageFeature;
import fr.siroz.cariboustonks.feature.garden.MouseLockFeature;
import fr.siroz.cariboustonks.feature.garden.pest.PestFinderFeature;
import fr.siroz.cariboustonks.feature.hunting.AttributeInfoTooltipFeature;
import fr.siroz.cariboustonks.feature.item.ColoredEnchantmentFeature;
import fr.siroz.cariboustonks.feature.item.TooltipDecoratorFeature;
import fr.siroz.cariboustonks.feature.keyshortcut.KeyShortcutFeature;
import fr.siroz.cariboustonks.feature.misc.HighlightMobFeature;
import fr.siroz.cariboustonks.feature.misc.PartyCommandFeature;
import fr.siroz.cariboustonks.feature.misc.REISearchBarCalculatorFeature;
import fr.siroz.cariboustonks.feature.reminders.BoosterCookieReminderFeature;
import fr.siroz.cariboustonks.feature.reminders.ChocolateLimitReminderFeature;
import fr.siroz.cariboustonks.feature.reminders.EnchantedCloakReminderFeature;
import fr.siroz.cariboustonks.feature.reminders.ForgeReminderFeature;
import fr.siroz.cariboustonks.feature.reminders.UbikCubeReminderFeature;
import fr.siroz.cariboustonks.feature.slayer.HighlightSlayerMobFeature;
import fr.siroz.cariboustonks.feature.slayer.SlayerCocoonedWarningFeature;
import fr.siroz.cariboustonks.feature.slayer.SlayerStatsFeature;
import fr.siroz.cariboustonks.feature.slayer.boss.TarantulaBossFeature;
import fr.siroz.cariboustonks.feature.stonks.StonksCommandFeature;
import fr.siroz.cariboustonks.feature.stonks.StonksFeature;
import fr.siroz.cariboustonks.feature.stonks.ItemValueViewerFeature;
import fr.siroz.cariboustonks.feature.stonks.tooltips.ItemValueTooltipFeature;
import fr.siroz.cariboustonks.feature.stonks.tooltips.auction.AuctionLowestBinTooltipFeature;
import fr.siroz.cariboustonks.feature.stonks.tooltips.bazaar.BazaarPriceTooltipFeature;
import fr.siroz.cariboustonks.feature.ui.AbiphoneFavoriteContactFeature;
import fr.siroz.cariboustonks.feature.ui.SelectedPetHighlightFeature;
import fr.siroz.cariboustonks.feature.ui.hud.DayHud;
import fr.siroz.cariboustonks.feature.ui.hud.FpsHud;
import fr.siroz.cariboustonks.feature.ui.hud.PingHud;
import fr.siroz.cariboustonks.feature.ui.hud.TpsHud;
import fr.siroz.cariboustonks.feature.ui.overlay.EtherWarpOverlayFeature;
import fr.siroz.cariboustonks.feature.ui.overlay.GyrokineticOverlayFeature;
import fr.siroz.cariboustonks.feature.vanilla.HideStatusEffectsFeature;
import fr.siroz.cariboustonks.feature.vanilla.MuteVanillaSoundFeature;
import fr.siroz.cariboustonks.feature.vanilla.ScrollableTooltipFeature;
import fr.siroz.cariboustonks.feature.vanilla.ZoomFeature;
import fr.siroz.cariboustonks.feature.waypoints.WaypointFeature;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Features {

	private static final Map<Class<? extends Feature>, Feature> FEATURE_INSTANCES = new ConcurrentHashMap<>();

	public Features() {
		// Chat
		registerFeature(new ChatColorationFeature());
		registerFeature(new ChatPositionFeature());
		registerFeature(new CopyChatMessageFeature());
		// Combat
		registerFeature(new CocoonedWarningFeature());
		registerFeature(new LowHealthWarningFeature());
		registerFeature(new RagnarockAxeFeature());
		registerFeature(new SecondLifeFeature());
		// Dungeons
		registerFeature(new CroesusMenuFeature());
		registerFeature(new ThornBossFeature());
		registerFeature(new SadanBossFeature());
		registerFeature(new NecronBossFeature());
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
		registerFeature(new HighlightMobFeature());
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
	public <T extends Feature> T getFeature(@NotNull Class<T> featureClass) {
		return (T) FEATURE_INSTANCES.get(featureClass);
	}

	private void registerFeature(@NotNull Feature feature) {
		FEATURE_INSTANCES.put(feature.getClass(), feature);

		CaribouStonks.managers().handleFeatureRegistration(feature);
	}

	private void postInitialize() {
		for (Feature feature : FEATURE_INSTANCES.values()) {
			feature.postInitialize(this);
		}
	}

	private void registerListeners() {
		ClientTickEvents.END_CLIENT_TICK.register(_mc -> onTick());
		ClientPlayConnectionEvents.JOIN.register((_h, _ps, _mc) -> onJoin());
	}

	private void onTick() {
		for (Feature feature : FEATURE_INSTANCES.values()) {
			feature.onClientTick();
		}
	}

	private void onJoin() {
		for (Feature feature : FEATURE_INSTANCES.values()) {
			feature.onClientJoinServer();
		}
	}
}
