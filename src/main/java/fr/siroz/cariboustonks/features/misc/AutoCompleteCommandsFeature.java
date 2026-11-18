package fr.siroz.cariboustonks.features.misc;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.feature.Feature;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockRegistry;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.HypixelDataSource;
import fr.siroz.cariboustonks.core.skyblock.data.hypixel.item.SkyBlockItemData;
import fr.siroz.cariboustonks.events.SkyBlockEvents;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.SharedSuggestionProvider;
import org.jspecify.annotations.Nullable;

public class AutoCompleteCommandsFeature extends Feature {
	private static final Command<FabricClientCommandSource> NO_OP = _ -> -1;

	public static @Nullable LiteralCommandNode<FabricClientCommandSource> warp;
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> gfs;
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> bz;
	public static @Nullable LiteralCommandNode<FabricClientCommandSource> call;

	// SIROZ-NOTE :: Certains items sont mal nommés, mais globalement 90% sont corrects, a tweak
	private final Set<String> bazaarItemNames = new HashSet<>();

	public AutoCompleteCommandsFeature() {
		// Le SkyBlockRegistry (et les données Bazaar) peuvent être remplis avant ou après
		// l'enregistrement des features : les nodes sont donc construits à l'arrivée formelle sur le SB.
		SkyBlockEvents.JOIN_EVENT.register(_ -> this.registerNodes());
		SkyBlockEvents.LEAVE_EVENT.register(this::clearNodes);
	}

	@Override
	public boolean isEnabled() {
		return this.skyBlock().location().onSkyBlock();
	}

	private void registerNodes() {
		try {
			var config = this.config().misc.autoCompleteCommands;
			if (!config.warp && !config.gfs && !config.bz && !config.call) {
				clearNodes();
				return;
			}

			if (bazaarItemNames.isEmpty()) {
				bazaarItemNames.addAll(this.loadBazaarItemNames());
			}

			warp = config.warp ? this.createWarpNode() : null;
			gfs = config.gfs ? this.createGfsNode(bazaarItemNames) : null;
			bz = config.bz ? this.createBzNode(bazaarItemNames) : null;
			call = config.call ? this.createCallNode() : null;
		} catch (Exception ex) {
			CaribouStonks.LOGGER.error("[{}] Failed to register the command nodes", this.getShortName(), ex);
		}
	}

	private void clearNodes() {
		warp = null;
		gfs = null;
		bz = null;
		call = null;
	}

	/**
	 * {@code /warp <destination>}
	 */
	private LiteralCommandNode<FabricClientCommandSource> createWarpNode() {
		return ClientCommands.literal("warp")
				.requires(_ -> this.isEnabled())
				.executes(NO_OP)
				.then(ClientCommands.argument("destination", StringArgumentType.greedyString())
						.suggests((_, builder) -> SharedSuggestionProvider.suggest(SkyBlockRegistry.getWarps(), builder))
						.executes(NO_OP))
				.build();
	}

	/**
	 * {@code /gfs <item> <amount>}
	 */
	private LiteralCommandNode<FabricClientCommandSource> createGfsNode(Set<String> itemNames) {
		return ClientCommands.literal("gfs")
				.requires(_ -> this.isEnabled())
				.executes(NO_OP)
				.then(ClientCommands.argument("item", StringArgumentType.greedyString())
						.suggests((_, builder) -> SharedSuggestionProvider.suggest(itemNames, builder))
						.then(ClientCommands.argument("amount", IntegerArgumentType.integer(0)))
						.executes(NO_OP)
				)
				.build();
	}

	/**
	 * {@code /bz <item>}
	 */
	private LiteralCommandNode<FabricClientCommandSource> createBzNode(Set<String> itemNames) {
		return ClientCommands.literal("bz")
				.requires(_ -> this.isEnabled())
				.executes(NO_OP)
				.then(ClientCommands.argument("item", StringArgumentType.greedyString())
						.suggests((_, builder) -> SharedSuggestionProvider.suggest(itemNames, builder))
						.executes(NO_OP))
				.build();
	}

	/**
	 * {@code /call <contact>}
	 */
	private LiteralCommandNode<FabricClientCommandSource> createCallNode() {
		return ClientCommands.literal("call")
				.requires(_ -> this.isEnabled())
				.executes(NO_OP)
				.then(ClientCommands.argument("contact", StringArgumentType.greedyString())
						.suggests((_, builder) -> SharedSuggestionProvider.suggest(
								SkyBlockRegistry.getAbiphoneContacts().stream()
										.flatMap(contact -> Stream.concat(
												Stream.of(contact.name().toLowerCase(Locale.ENGLISH)),
												contact.callNames().stream()))
										.distinct(), // certains ont déjà leur nom dans leurs callNames
								builder))
						.executes(NO_OP))
				.build();
	}

	private Set<String> loadBazaarItemNames() {
		HypixelDataSource dataSource = this.skyBlock().getHypixelDataSource();
		Set<String> names = new HashSet<>();
		for (String bazaarId : dataSource.getBazaarItems()) {
			SkyBlockItemData itemData = dataSource.getSkyBlockItem(bazaarId);
			if (itemData != null) {
				names.add(itemData.name());
			}
		}
		return Set.copyOf(names);
	}
}
