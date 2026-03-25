package fr.siroz.cariboustonks.features.ui.deployable;

import fr.siroz.cariboustonks.core.skyblock.item.HeadTextures;
import fr.siroz.cariboustonks.util.ItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

enum Deployable {
	MANA_FLUX_POWER_ORB("Mana Flux",
			Type.COMBAT,
			1,
			18,
			null,
			ItemUtils.createSkull(HeadTextures.MANA_FLUX)
	),
	OVERFLUX_POWER_ORB("Overflux",
			Type.COMBAT,
			2,
			18,
			null,
			ItemUtils.createSkull(HeadTextures.OVERFLUX)
	),
	PLASMAFLUX_POWER_ORB("Plasmaflux",
			Type.COMBAT,
			3,
			20,
			null,
			ItemUtils.createSkull(HeadTextures.PLASMAFLUX)
	),
	ALERT_FLARE("Alert Flare",
			Type.FLARE,
			1,
			40,
			Component.literal("Alert Flare").withStyle(ChatFormatting.BLUE),
			new ItemStack(Items.FIREWORK_ROCKET)
	),
	SOS_FLARE("SOS Flare",
			Type.FLARE,
			2,
			40,
			Component.literal("SOS Flare").withStyle(ChatFormatting.DARK_PURPLE),
			new ItemStack(Items.FIREWORK_ROCKET)
	),
	UMBERELLA("Umberella",
			Type.FISHING,
			1,
			30,
			null,
			ItemUtils.createSkull(HeadTextures.UMBERELLA)
	),
	BLACK_HOLE("Black Hole",
			Type.PERSONAL,
			1,
			50,
			null,
			ItemUtils.createSkull(HeadTextures.BLACK_HOLE)
	),
	TITANIUM_LANTERN("Titanium Lantern",
			Type.MINING,
			1,
			30,
			null,
			ItemUtils.createSkull(HeadTextures.TITANIUM_LANTERN)
	),
	GLACITE_LANTERN("Glacite Lantern",
			Type.MINING,
			2,
			30,
			null,
			ItemUtils.createSkull(HeadTextures.GLACITE_LANTERN)
	),
	WILL_O_WISP("Will-o'-wisp",
			Type.MINING,
			3,
			30,
			null,
			ItemUtils.createSkull(HeadTextures.WILL_O_WISP)
	),
	;

	private final String name;
	private final Type type;
	private final int priority;
	private final int blockRange;
	private final @Nullable Component displayName;
	private final ItemStack itemDisplay;

	public static final Deployable[] VALUES = Deployable.values();

	Deployable(
			String name,
			Type type,
			int priority,
			int blockRange,
			@Nullable Component displayName,
			ItemStack itemDisplay
	) {
		this.name = name;
		this.type = type;
		this.priority = priority;
		this.blockRange = blockRange;
		this.displayName = displayName;
		this.itemDisplay = itemDisplay;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public int getPriority() {
		return priority;
	}

	public int getBlockRange() {
		return blockRange;
	}

	public @Nullable Component getDisplayName() {
		return displayName;
	}

	public ItemStack getItemDisplay() {
		return itemDisplay;
	}

	public enum Type {
		COMBAT,
		FLARE,
		FISHING,
		MINING,
		PERSONAL
	}
}
