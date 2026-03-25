package fr.siroz.cariboustonks.feature.ui.deployable;

import fr.siroz.cariboustonks.util.HeadTextures;
import fr.siroz.cariboustonks.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

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
			Text.literal("Alert Flare").formatted(Formatting.BLUE),
			new ItemStack(Items.FIREWORK_ROCKET)
	),
	SOS_FLARE("SOS Flare",
			Type.FLARE,
			2,
			40,
			Text.literal("SOS Flare").formatted(Formatting.DARK_PURPLE),
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
	private final @Nullable Text displayName;
	private final ItemStack itemDisplay;

	public static final Deployable[] VALUES = Deployable.values();

	Deployable(
			String name,
			Type type,
			int priority,
			int blockRange,
			@Nullable Text displayName,
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

	public @Nullable Text getDisplayName() {
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
