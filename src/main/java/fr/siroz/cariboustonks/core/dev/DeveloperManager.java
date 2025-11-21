package fr.siroz.cariboustonks.core.dev;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.JsonOps;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.json.GsonProvider;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.core.skyblock.SkyBlockAPI;
import fr.siroz.cariboustonks.event.EventHandler;
import fr.siroz.cariboustonks.event.NetworkEvents;
import fr.siroz.cariboustonks.event.RenderEvents;
import fr.siroz.cariboustonks.event.WorldEvents;
import fr.siroz.cariboustonks.util.Client;
import fr.siroz.cariboustonks.util.DeveloperTools;
import fr.siroz.cariboustonks.util.InventoryUtils;
import fr.siroz.cariboustonks.util.ItemUtils;
import fr.siroz.cariboustonks.util.TimeUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Internal Developer Manager.
 * <p>
 * Class initialized only in a development environment
 */
@ApiStatus.Internal
public final class DeveloperManager {

	private final Object2IntMap<ArmorStand> texturedArmorStands = new Object2IntOpenHashMap<>();
	private boolean dumpSound = false;

	public DeveloperManager() {
		CaribouStonks.LOGGER.warn("Debug mode enabled ({}) {}", SharedConstants.getCurrentVersion().name(), DeveloperTools.isSnapshot() ? "(Snapshot)" : "");
		// Debug Renderer
		DebugRenderer debugRenderer = new DebugRenderer(this);
		// Events
		ClientPlayConnectionEvents.JOIN.register(this::onPlayConnection);
		WorldEvents.ALLOW_SOUND.register(this::onSound);
		NetworkEvents.PLAY_SOUND_PACKET.register(this::onSoundPacket);
		RenderEvents.WORLD_RENDER.register(debugRenderer::render);
		// Commands
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _ra) -> dispatcher.register(
				ClientCommandManager.literal(CaribouStonks.NAMESPACE).then(ClientCommandManager.literal("devtools")
						.then(dumpSoundCommand())
						.then(dumpMayorCommand())
						.then(dumpHeldItemSimpleCommand())
						.then(dumpHeldItemCommand())
						.then(dumpArmorStandHeadTextures()))
		));
	}

	Object2IntMap<ArmorStand> getTexturedArmorStands() {
		return texturedArmorStands;
	}

	@EventHandler(event = "ClientPlayConnectionEvents.JOIN")
	private void onPlayConnection(ClientPacketListener _handler, PacketSender _sender, Minecraft _c) {
		Client.sendMessageWithPrefix(Component.literal("Debug mode enabled (" + SharedConstants.getCurrentVersion().name() + ")").withStyle(ChatFormatting.RED)
				.append(DeveloperTools.isSnapshot() ? Component.literal(" (Snapshot)").withStyle(ChatFormatting.DARK_RED) : Component.empty()));
	}

	@EventHandler(event = "WorldEvents.ALLOW_SOUND")
	private boolean onSound(@NotNull SoundEvent soundEvent) {
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
			String soundId = packet.getSound().value().location().getPath();
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

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemSimpleCommand() {
		return ClientCommandManager.literal("dumpHeldItemSimple").executes(ctx -> {
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
		return ClientCommandManager.literal("dumpHeldItem").executes(ctx -> {
			String json = GsonProvider.standard().toJson(ItemStack.CODEC.encodeStart(
					DeveloperTools.getRegistryLookup().createSerializationContext(JsonOps.INSTANCE),
					ctx.getSource().getPlayer().getMainHandItem()).getOrThrow());

			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Component.literal("Debug Held Item: " + json)));
			return Command.SINGLE_SUCCESS;
		});
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpArmorStandHeadTextures() {
		return ClientCommandManager.literal("dumpArmorStandHeadTextures").executes(ctx -> {
			texturedArmorStands.clear();
			TickScheduler.getInstance().runLater(texturedArmorStands::clear, 20 * 10);

			List<ArmorStand> armorStands = ctx.getSource().getWorld().getEntitiesOfClass(
					ArmorStand.class,
					ctx.getSource().getPlayer().getBoundingBox().inflate(8d),
					EntitySelector.ENTITY_NOT_BEING_RIDDEN
			);

			int id = 0;
			for (ArmorStand armorStand : armorStands) {
				texturedArmorStands.put(armorStand, id);
				Iterable<ItemStack> equippedItems = InventoryUtils.getArmorFromEntity(armorStand);

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
		return ClientCommandManager.literal("dumpSound").executes(ctx -> {
			dumpSound = !dumpSound;
			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Component.literal("Dump sound: " + dumpSound)));
			return Command.SINGLE_SUCCESS;
		});
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpMayorCommand() {
		return ClientCommandManager.literal("dumpMayor").executes(ctx -> {
			String str = CaribouStonks.core().getHypixelDataSource().getElection() != null
					? CaribouStonks.core().getHypixelDataSource().getElection().toString()
					: "NULL";
			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Component.literal("--")));
			ctx.getSource().sendFeedback(Component.literal(str));
			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Component.literal("--")));
			return Command.SINGLE_SUCCESS;
		});
	}
}
