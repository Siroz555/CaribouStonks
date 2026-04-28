package fr.siroz.cariboustonks.feature.fishing;

import fr.siroz.cariboustonks.core.skyblock.IslandType;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

/**
 * Represents all Rare Sea Creatures. (Legendary+)
 */
public enum RareSeaCreature {
	// Water
	CARROT_KING("Carrot King", "Is this even a fish? It's the Carrot King!", Formatting.AQUA, false),
	WATER_HYDRA("Water Hydra", "The Water Hydra has come to test your strength.", Formatting.BLUE, false),
	// Water - Galatea
	THE_LOCH_EMPEROR("The Loch Emperor", "The Loch Emperor arises from the depths.", Formatting.RED, true, IslandType.GALATEA),
	NESSIE("Nessie", "You've caused a disturbance in the loch. Could it be... Nessie?", Formatting.LIGHT_PURPLE, true, IslandType.GALATEA),
	// Water - Jerry Workshop
	YETI("Yeti", "What is this creature!?", Formatting.WHITE, true, IslandType.JERRY_WORKSHOP),
	REINDRAKE("Reindrake", "A Reindrake forms from the depths.", Formatting.RED, false, IslandType.JERRY_WORKSHOP),
	// Water - Crystal Hollows
	ABYSSAL_MINER("Abyssal Miner", "An Abyssal Miner breaks out of the water!", Formatting.AQUA, true, IslandType.CRYSTAL_HOLLOWS),
	// Water - Fishing Festival
	GREAT_WHITE_SHARK("Great White Shark", "Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood!", Formatting.AQUA, false),
	// Water - Spooky Event
	WEREWOLF("Werewolf", "It must be a full moon, a Werewolf appears.", Formatting.GRAY, false),
	PHANTOM_FISHER("Phantom Fisher", "The spirit of a long lost Phantom Fisher has come to haunt you.", Formatting.DARK_PURPLE, true),
	GRIM_REAPER("Grim Reaper", "This can't be! The manifestation of death himself!", Formatting.DARK_PURPLE, true),
	// Water - Backwater Bayou
	ALLIGATOR("Alligator", "A long snout breaks the surface of the water. It's an Alligator!", Formatting.DARK_GREEN, false),
	BLUE_RINGED_OCTOPUS("Blue Ringed Octopus", "A garish set of tentacles arise. It's a Blue Ringed Octopus!", Formatting.LIGHT_PURPLE, false),
	TITANOBOA("Titanoboa", "A massive Titanoboa surfaces. Its body stretches as far as the eye can see.", Formatting.DARK_GREEN, false),
	WIKI_TIKI("Wiki Tiki", "The water bubbles and froths. A massive form emerges- you have disturbed the Wiki Tiki! You shall pay the price.", Formatting.LIGHT_PURPLE, false),
	// Lava - Crimson Isle
	FIERY_SCUTTLER("Fiery Scuttler", "A Fiery Scuttler inconspicuously waddles up to you, friends in tow.", Formatting.GOLD, false, IslandType.CRIMSON_ISLE),
	THUNDER("Thunder", "You hear a massive rumble as Thunder emerges.", Formatting.AQUA, true, IslandType.CRIMSON_ISLE),
	LORD_JAWBUS("Lord Jawbus", "You have angered a legendary creature... Lord Jawbus has arrived.", Formatting.RED, true, IslandType.CRIMSON_ISLE),
	RAGNAROK("Ragnarok", "The sky darkens and the air thickens. The end times are upon us: Ragnarok is here.", Formatting.DARK_RED, false, IslandType.CRIMSON_ISLE),
	PLHLEGBLAST("Plhlegblast", "WOAH! A Plhlegblast appeared.", Formatting.DARK_RED, true, IslandType.CRIMSON_ISLE),
	;

	private static final Map<String, RareSeaCreature> SEA_CREATURE_CHAT = Arrays.stream(values())
			.collect(Collectors.toUnmodifiableMap(RareSeaCreature::getChatMessage, Function.identity()));

	private final String name;
	private final String chatMessage;
	private final Formatting color;
	private final boolean highlightable;
	private final IslandType islandType;

	RareSeaCreature(String name, String chatMessage, Formatting color, boolean highlightable) {
		this(name, chatMessage, color, highlightable, IslandType.ANY);
	}

	RareSeaCreature(String name, String chatMessage, Formatting color, boolean highlightable, IslandType islandType) {
		this.name = name;
		this.chatMessage = chatMessage;
		this.color = color;
		this.highlightable = highlightable;
		this.islandType = islandType;
	}

	public static @Nullable RareSeaCreature fromChatMessage(String chatMessage) {
		return SEA_CREATURE_CHAT.get(chatMessage);
	}

	public String getName() {
		return name;
	}

	public String getChatMessage() {
		return chatMessage;
	}

	public boolean isHighlightable() {
		return highlightable;
	}

	public Formatting getColor() {
		return color;
	}

	public IslandType getIslandType() {
		return islandType;
	}

	public Text getText() {
		return Text.literal(name).formatted(color, Formatting.BOLD);
	}
}
