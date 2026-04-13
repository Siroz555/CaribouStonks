package fr.siroz.cariboustonks.core.mod.dev;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.JsonOps;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.service.json.GsonProvider;
import fr.siroz.cariboustonks.core.service.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.core.skyblock.tablist.TabLine;
import fr.siroz.cariboustonks.core.skyblock.tablist.TabWidget;
import fr.siroz.cariboustonks.events.EventHandler;
import fr.siroz.cariboustonks.events.NetworkEvents;
import fr.siroz.cariboustonks.events.RenderEvents;
import fr.siroz.cariboustonks.events.WorldEvents;
import fr.siroz.cariboustonks.rendering.world.WorldRenderer;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.TimeUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

/**
 * Internal Developer Manager.
 * <p>
 * Class initialized only in a development environment
 */
public final class DeveloperManager {

	private final Object2IntMap<ArmorStand> texturedArmorStands = new Object2IntOpenHashMap<>();
	private boolean dumpSound = false;

	public DeveloperManager() {
		CaribouStonks.LOGGER.warn("Debug mode enabled ({}) {}", SharedConstants.getCurrentVersion().name(), DeveloperTools.isSnapshot() ? "(Snapshot)" : "");
		// Debug Renderer
		DebugWorldRenderer renderer = new DebugWorldRenderer();
		RenderEvents.WORLD_RENDER_EVENT.register(renderer::render);
		// Events
		ClientPlayConnectionEvents.JOIN.register(this::onPlayConnection);
		WorldEvents.ALLOW_SOUND_EVENT.register(this::onSound);
		NetworkEvents.PLAY_SOUND_PACKET.register(this::onSoundPacket);
		RenderEvents.WORLD_RENDER_EVENT.register(this::onWorldRender);
		// Commands
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _) -> dispatcher.register(
				ClientCommands.literal(CaribouStonks.NAMESPACE).then(ClientCommands.literal("devtools")
						.then(dumpSoundCommand())
						.then(dumpMayorCommand())
						.then(dumpTabListCommand())
						.then(dumpHeldItemSimpleCommand())
						.then(dumpHeldItemCommand())
						.then(dumpArmorStandHeadTextures()))
		));
	}

	@EventHandler(event = "ClientPlayConnectionEvents.JOIN")
	private void onPlayConnection(ClientPacketListener _handler, PacketSender _sender, Minecraft _c) {
		Client.sendMessageWithPrefix(Component.literal("Debug mode enabled (" + SharedConstants.getCurrentVersion().name() + ")").withStyle(ChatFormatting.RED)
				.append(DeveloperTools.isSnapshot() ? Component.literal(" (Snapshot)").withStyle(ChatFormatting.DARK_RED) : Component.empty()));
	}

	@EventHandler(event = "WorldEvents.ALLOW_SOUND")
	private boolean onSound(@NonNull SoundEvent soundEvent) {
		if (dumpSound) {
			String soundId = soundEvent.location().getPath();
			String time = TimeUtils.formatInstant(Instant.now(), TimeUtils.TIME_HH_MM_SS);
			Client.sendMessage(Component.literal("(Client) " + time + " :: " + soundId));
		}
		return true;
	}

	@EventHandler(event = "NetworkEvents.PLAY_SOUND_PACKET")
	private void onSoundPacket(ClientboundSoundPacket packet) {
		if (dumpSound) {
			String soundId = Client.convertSoundPacketToName(packet);
			String time = TimeUtils.formatInstant(Instant.now(), TimeUtils.TIME_HH_MM_SS);
			String pitch = BigDecimal.valueOf(packet.getPitch())
					.setScale(3, RoundingMode.DOWN)
					.stripTrailingZeros()
					.toPlainString();
			String volume = BigDecimal.valueOf(packet.getVolume())
					.setScale(3, RoundingMode.DOWN)
					.stripTrailingZeros()
					.toPlainString();
			Client.sendMessage(Component.literal("(Server) " + time + " :: " + soundId + " Pitch: " + pitch + " Volume: " + volume));
		}
	}

	@EventHandler(event = "RenderEvents.WORLD_RENDER_EVENT")
	private void onWorldRender(WorldRenderer renderer) {
		if (!texturedArmorStands.isEmpty()) {
			for (Object2IntMap.Entry<ArmorStand> armorStand : texturedArmorStands.object2IntEntrySet()) {
				ArmorStand entity = armorStand.getKey();
				if (entity == null) continue;

				Vec3 centerPos = entity.position();
				renderer.submitText(Component.literal("#" + armorStand.getIntValue()),
						centerPos.add(0, 1, 0), 1, true);
			}
		}
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemSimpleCommand() {
		return ClientCommands.literal("dumpHeldItemSimple").executes(ctx -> {
			ItemStack item = ctx.getSource().getPlayer().getMainHandItem();
			if (item.isEmpty()) {
				ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Component.literal("Item is null or empty").withStyle(ChatFormatting.RED)));
			} else {
				MutableComponent message = Component.empty();
				message.append(item.getHoverName()).append(" :")
						.append("\n")
						.append(" - SkyBlockItemId: " + SkyBlockAPI.getSkyBlockItemId(item)).withStyle(ChatFormatting.GRAY)
						.append("\n")
						.append(" - SkyBlockApiId: " + SkyBlockAPI.getSkyBlockApiId(item)).withStyle(ChatFormatting.GRAY);

				ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(message));
			}
			return Command.SINGLE_SUCCESS;
		});
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemCommand() {
		return ClientCommands.literal("dumpHeldItem").executes(ctx -> {
			String json = GsonProvider.standard().toJson(ItemStack.CODEC.encodeStart(
					DeveloperTools.getRegistryLookup().createSerializationContext(JsonOps.INSTANCE),
					ctx.getSource().getPlayer().getMainHandItem()).getOrThrow());

			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Component.literal("Debug Held Item: " + json)));
			return Command.SINGLE_SUCCESS;
		});
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpArmorStandHeadTextures() {
		return ClientCommands.literal("dumpArmorStandHeadTextures").executes(ctx -> {
			texturedArmorStands.clear();
			TickScheduler.getInstance().runLater(texturedArmorStands::clear, 20 * 10);

			List<ArmorStand> armorStands = ctx.getSource().getLevel().getEntitiesOfClass(
					ArmorStand.class,
					ctx.getSource().getPlayer().getBoundingBox().inflate(8d),
					EntitySelector.ENTITY_NOT_BEING_RIDDEN
			);

			int id = 0;
			for (ArmorStand armorStand : armorStands) {
				texturedArmorStands.put(armorStand, id);
				Iterable<ItemStack> equippedItems = Client.getArmorFromEntity(armorStand);

				ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Component.literal("Head texture #" + id + ": ")));

				for (ItemStack stack : equippedItems) {
					ItemUtils.getHeadTextureOptional(stack).ifPresent(texture -> ctx.getSource().sendFeedback(Component.nullToEmpty(texture)));
				}
				id++;
			}
			return Command.SINGLE_SUCCESS;
		});
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpSoundCommand() {
		return ClientCommands.literal("dumpSound").executes(ctx -> {
			dumpSound = !dumpSound;
			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Component.literal("Dump sound: " + dumpSound)));
			return Command.SINGLE_SUCCESS;
		});
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpMayorCommand() {
		return ClientCommands.literal("dumpMayor").executes(ctx -> {
			String str = CaribouStonks.skyBlock().getHypixelDataSource().getElection() != null
					? CaribouStonks.skyBlock().getHypixelDataSource().getElection().toString()
					: "NULL";
			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Component.literal("--")));
			ctx.getSource().sendFeedback(Component.literal(str));
			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Component.literal("--")));
			return Command.SINGLE_SUCCESS;
		});
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpTabListCommand() {
		return ClientCommands.literal("dumpTabList").executes(ctx -> {
			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Component.literal("-- DUMP TAB --")));
			for (TabWidget widget : CaribouStonks.skyBlock().getTabListManager().getWidgetView()) {
				ctx.getSource().sendFeedback(widget.getHeader().component());
				for (TabLine line : widget.getLines()) {
					ctx.getSource().sendFeedback(line.component());
				}
			}
			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Component.literal("--")));
			return Command.SINGLE_SUCCESS;
		});
	}
}
