package fr.siroz.cariboustonks.features.ui;

import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.events.ChatEvents;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.util.render.AnimationUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("checkstyle:linelength")
public class RareDropVisualEffectFeature extends Feature {

	private static final Pattern RARE_DROP_PATTERN = Pattern.compile("^(?!.*:)(?:RARE|VERY RARE|CRAZY RARE|INSANE) DROP!\\s+\\(?(?<item>.+?)\\)?(?:\\s+\\(\\+\\d+%? ✯ Magic Find\\))?$");
	private static final Map<String, String> RARE_DROP_ITEMS = new HashMap<>();

	public RareDropVisualEffectFeature() {
		ChatEvents.MESSAGE_RECEIVE_EVENT.register(this::onChatMessage);
	}

	@Override
	public boolean isEnabled() {
		return SkyBlockAPI.isOnSkyBlock() && this.config().uiAndVisuals.rareDropVisualEffect;
	}

	@EventHandler(event = "ChatEvents.MESSAGE_RECEIVE_EVENT")
	private void onChatMessage(@NonNull Component component) {
		if (!isEnabled()) return;

		String message = component.getString();
		try {
			Matcher rareDropMatcher = RARE_DROP_PATTERN.matcher(message);
			if (rareDropMatcher.matches()) {
				handleEffect(rareDropMatcher.group("item"));
			}
		} catch (Exception ex) {
			CaribouStonks.LOGGER.error("[{}] unable to handle rare drop for {}", getShortName(), message, ex);
		}
	}

	private void handleEffect(@Nullable String item) {
		if (item == null || item.isBlank()) return;

		ItemStack itemStack = parseItemStack(item);
		if (itemStack != null && !itemStack.isEmpty() && !itemStack.is(Items.BARRIER)) {
			AnimationUtils.showSpecialEffect(itemStack, ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER_OMINOUS, 10);
		}
	}

	private @Nullable ItemStack parseItemStack(@NonNull String item) {
		String skyBlockItemId = RARE_DROP_ITEMS.getOrDefault(item, null);
		if (skyBlockItemId == null) return null;

		return CaribouStonks.skyBlock().getHypixelDataSource().getItemStack(skyBlockItemId);
	}

	static {
		// Mining - Mineshaft
		RARE_DROP_ITEMS.put("Littlefoot Fluff", "LITTLEFOOT_FLUFF");
		// Fishing
		RARE_DROP_ITEMS.put("Radioactive Vial", "RADIOACTIVE_VIAL");
		RARE_DROP_ITEMS.put("Titanoboa Shed", "TITANOBOA_SHED");
		RARE_DROP_ITEMS.put("Tiki Mask", "TIKI_MASK");
		// Mythological Ritual
		RARE_DROP_ITEMS.put("Enchanted Book (Chimera I)", "ENCHANTMENT_ULTIMATE_CHIMERA_1");
		RARE_DROP_ITEMS.put("Minos Relic", "MINOS_RELIC");
		RARE_DROP_ITEMS.put("Fateful Stinger", "FATEFUL_STINGER");
		RARE_DROP_ITEMS.put("Manti-core", "MANTI_CORE");
		RARE_DROP_ITEMS.put("Shimmering Wool", "SHIMMERING_WOOL");
		// Slayer - Zombie
		RARE_DROP_ITEMS.put("Scythe Blade", "SCYTHE_BLADE");
		RARE_DROP_ITEMS.put("Shard Of The Shredded", "SHARD_OF_THE_SHREDDED");
		RARE_DROP_ITEMS.put("Severed Hand", "SEVERED_HAND");
		RARE_DROP_ITEMS.put("Warden Heart", "WARDEN_HEART");
		// Slayer - Spider
		RARE_DROP_ITEMS.put("Shriveled Wasp", "SHRIVELED_WASP");
		RARE_DROP_ITEMS.put("Ensnared Snail", "ENSNARED_SNAIL");
		RARE_DROP_ITEMS.put("Digested Mosquito", "DIGESTED_MOSQUITO");
		RARE_DROP_ITEMS.put("Primordial Eye", "PRIMORDIAL_EYE");
		// Slayer - Guardian ???
		// Slayer - Wolf
		RARE_DROP_ITEMS.put("Overflux Capacitor", "OVERFLUX_CAPACITOR");
		// Slayer - Enderman
		RARE_DROP_ITEMS.put("Summoning Eye", "SUMMONING_EYE");
		RARE_DROP_ITEMS.put("End Stone Idol", "ENDSTONE_IDOL");
		RARE_DROP_ITEMS.put("Judgement Core", "JUDGEMENT_CORE");
		// Slayer - Blaze
		RARE_DROP_ITEMS.put("Duplex I", "ENCHANTMENT_ULTIMATE_REITERATE_1");
		// l'API d'Hypixel retourne le mauvais skin pour le dice :c
		RARE_DROP_ITEMS.put("High Class Archfiend Dice", "HIGH_CLASS_ARCHFIEND_DICE");
		// Slayer - Vampire
		RARE_DROP_ITEMS.put("McGrubber's Burger", "MCGRUBBER_BURGER");
		RARE_DROP_ITEMS.put("Unfanged Vampire Part", "UNFANGED_VAMPIRE_PART");
		RARE_DROP_ITEMS.put("Enchanted Book Bundle", "ENCHANTED_BOOK_BUNDLE_THE_ONE");
	}
}
