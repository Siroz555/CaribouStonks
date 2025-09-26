package fr.siroz.cariboustonks.feature;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.feature.chat.ChatColorationFeature;
import fr.siroz.cariboustonks.feature.chat.ChatPositionFeature;
import fr.siroz.cariboustonks.feature.chat.CopyChatMessageFeature;
import fr.siroz.cariboustonks.feature.combat.CocoonedWarningFeature;
import fr.siroz.cariboustonks.feature.combat.LowHealthWarningFeature;
import fr.siroz.cariboustonks.feature.diana.MythologicalRitualFeature;
import fr.siroz.cariboustonks.feature.fishing.BobberTimerFeature;
import fr.siroz.cariboustonks.feature.fishing.FishCaughtFeature;
import fr.siroz.cariboustonks.feature.fishing.hotspot.HotspotFeature;
import fr.siroz.cariboustonks.feature.fishing.radar.HotspotRadarFeature;
import fr.siroz.cariboustonks.feature.foraging.BreakTreeAnimationFeature;
import fr.siroz.cariboustonks.feature.foraging.TreeOverlayFeature;
import fr.siroz.cariboustonks.feature.garden.MouseLockFeature;
import fr.siroz.cariboustonks.feature.garden.pest.PestFinderFeature;
import fr.siroz.cariboustonks.feature.hunting.AttributeInfoTooltipFeature;
import fr.siroz.cariboustonks.feature.item.ColoredEnchantmentFeature;
import fr.siroz.cariboustonks.feature.item.ScrollableTooltipFeature;
import fr.siroz.cariboustonks.feature.item.TooltipDecoratorFeature;
import fr.siroz.cariboustonks.feature.keyshortcut.KeyShortcutFeature;
import fr.siroz.cariboustonks.feature.misc.HighlightMobFeature;
import fr.siroz.cariboustonks.feature.misc.PartyCommandFeature;
import fr.siroz.cariboustonks.feature.misc.REISearchBarCalculatorFeature;
import fr.siroz.cariboustonks.feature.misc.StopPickobulusAbilityFeature;
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
import fr.siroz.cariboustonks.feature.stonks.tooltips.auction.AuctionLowestBinTooltipFeature;
import fr.siroz.cariboustonks.feature.stonks.tooltips.bazaar.BazaarPriceTooltipFeature;
import fr.siroz.cariboustonks.feature.ui.AbiphoneFavoriteContactFeature;
import fr.siroz.cariboustonks.feature.ui.SelectedPetHighlightFeature;
import fr.siroz.cariboustonks.feature.ui.ZoomFeature;
import fr.siroz.cariboustonks.feature.ui.hud.DayHud;
import fr.siroz.cariboustonks.feature.ui.hud.FpsHud;
import fr.siroz.cariboustonks.feature.ui.hud.PingHud;
import fr.siroz.cariboustonks.feature.ui.hud.TpsHud;
import fr.siroz.cariboustonks.feature.ui.overlay.EtherWarpOverlayFeature;
import fr.siroz.cariboustonks.feature.ui.overlay.GyrokineticOverlayFeature;
import fr.siroz.cariboustonks.feature.vanilla.MuteVanillaSoundFeature;
import fr.siroz.cariboustonks.feature.waypoints.WaypointFeature;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class Features {

	private static final Set<Feature> FEATURES = new LinkedHashSet<>();
	private static final Map<Class<? extends Feature>, Feature> FEATURE_INSTANCES = new ConcurrentHashMap<>();

    public Features() {
		CaribouStonks.LOGGER.info("[FeatureManager] Loading..");
        // Chat
		registerFeature(new ChatColorationFeature());
        registerFeature(new ChatPositionFeature());
		registerFeature(new CopyChatMessageFeature());
		// Combat
		registerFeature(new CocoonedWarningFeature());
		registerFeature(new LowHealthWarningFeature());
		// Events
		registerFeature(new MythologicalRitualFeature());
		// Fishing
		registerFeature(new BobberTimerFeature());
		registerFeature(new FishCaughtFeature());
		registerFeature(new HotspotFeature());
		registerFeature(new HotspotRadarFeature());
		// Foraging
		registerFeature(new BreakTreeAnimationFeature());
		registerFeature(new TreeOverlayFeature());
		// Garden
		registerFeature(new PestFinderFeature());
		registerFeature(new MouseLockFeature());
		// Hunting
		registerFeature(new AttributeInfoTooltipFeature(3));
		// Item
		registerFeature(new ColoredEnchantmentFeature());
		registerFeature(new ScrollableTooltipFeature());
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
		registerFeature(new StonksCommandFeature());
		registerFeature(new StonksFeature());
		registerFeature(new BazaarPriceTooltipFeature(1));
		registerFeature(new AuctionLowestBinTooltipFeature(2));
		// UI
		registerFeature(new AbiphoneFavoriteContactFeature(0));
		registerFeature(new ZoomFeature());
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
		registerFeature(new StopPickobulusAbilityFeature());
		// Vanilla
		registerFeature(new MuteVanillaSoundFeature());

		CaribouStonks.LOGGER.info("{} features are now loaded and ready", getFeatures().size());
    }

	private void registerFeature(@NotNull Feature feature) {
		FEATURES.add(feature);
		FEATURE_INSTANCES.put(feature.getClass(), feature);

		CaribouStonks.managers().handleFeatureRegistration(feature);
	}

	@Contract(" -> new")
	public @NotNull @Unmodifiable Set<Feature> getFeatures() {
		return new LinkedHashSet<>(FEATURES);
	}

	@SuppressWarnings("unchecked")
	public <T extends Feature> T getFeature(@NotNull Class<T> featureClass) {
		return (T) FEATURE_INSTANCES.get(featureClass);
	}
}
