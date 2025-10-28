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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Internal Developer Manager.
 * <p>
 * Class initialized only in a development environment
 */
@ApiStatus.Internal
public final class DeveloperManager {

	private final Object2IntMap<ArmorStandEntity> texturedArmorStands = new Object2IntOpenHashMap<>();
	private boolean dumpSound = false;

	public DeveloperManager() {
		CaribouStonks.LOGGER.warn("Debug mode enabled ({}) {}", SharedConstants.getGameVersion().name(), DeveloperTools.isSnapshot() ? "(Snapshot)" : "");
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

	Object2IntMap<ArmorStandEntity> getTexturedArmorStands() {
		return texturedArmorStands;
	}

	@EventHandler(event = "ClientPlayConnectionEvents.JOIN")
	private void onPlayConnection(ClientPlayNetworkHandler _handler, PacketSender _sender, MinecraftClient _c) {
		Client.sendMessageWithPrefix(Text.literal("Debug mode enabled (" + SharedConstants.getGameVersion().name() + ")").formatted(Formatting.RED)
				.append(DeveloperTools.isSnapshot() ? Text.literal(" (Snapshot)").formatted(Formatting.DARK_RED) : Text.empty()));
	}

	@EventHandler(event = "WorldEvents.ALLOW_SOUND")
	private boolean onSound(@NotNull SoundEvent soundEvent) {
		if (dumpSound) {
			String soundId = soundEvent.id().getPath();
			String time = TimeUtils.formatInstant(Instant.now(), TimeUtils.TIME_HH_MM_SS);
			Client.sendMessage(Text.literal("(Client) " + time + " :: " + soundId));
		}
		return true;
	}

	@EventHandler(event = "NetworkEvents.PLAY_SOUND_PACKET")
	private void onSoundPacket(PlaySoundS2CPacket packet) {
		if (dumpSound) {
			String soundId = packet.getSound().value().id().getPath();
			String time = TimeUtils.formatInstant(Instant.now(), TimeUtils.TIME_HH_MM_SS);
			String pitch = BigDecimal.valueOf(packet.getPitch())
					.setScale(3, RoundingMode.DOWN)
					.stripTrailingZeros()
					.toPlainString();
			String volume = BigDecimal.valueOf(packet.getVolume())
					.setScale(3, RoundingMode.DOWN)
					.stripTrailingZeros()
					.toPlainString();
			Client.sendMessage(Text.literal("(Server) " + time + " :: " + soundId + " Pitch: " + pitch + " Volume: " + volume));
		}
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemSimpleCommand() {
		return ClientCommandManager.literal("dumpHeldItemSimple").executes(ctx -> {
			ItemStack item = ctx.getSource().getPlayer().getMainHandStack();
			if (item == null || item.isEmpty()) {
				ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Text.literal("Item is null or empty").formatted(Formatting.RED)));
			} else {
				MutableText message = Text.empty();
				message.append(item.getName()).append(" :")
						.append("\n")
						.append(" - SkyBlockItemId: " + SkyBlockAPI.getSkyBlockItemId(item)).formatted(Formatting.GRAY)
						.append("\n")
						.append(" - SkyBlockApiId: " + SkyBlockAPI.getSkyBlockApiId(item)).formatted(Formatting.GRAY);

				ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(message));
			}
			return Command.SINGLE_SUCCESS;
		});
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemCommand() {
		return ClientCommandManager.literal("dumpHeldItem").executes(ctx -> {
			String json = GsonProvider.standard().toJson(ItemStack.CODEC.encodeStart(
					DeveloperTools.getRegistryLookup().getOps(JsonOps.INSTANCE),
					ctx.getSource().getPlayer().getMainHandStack()).getOrThrow());

			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Text.literal("Debug Held Item: " + json)));
			return Command.SINGLE_SUCCESS;
		});
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpArmorStandHeadTextures() {
		return ClientCommandManager.literal("dumpArmorStandHeadTextures").executes(ctx -> {
			texturedArmorStands.clear();
			TickScheduler.getInstance().runLater(texturedArmorStands::clear, 20 * 10);

			List<ArmorStandEntity> armorStands = ctx.getSource().getWorld().getEntitiesByClass(
					ArmorStandEntity.class,
					ctx.getSource().getPlayer().getBoundingBox().expand(8d),
					EntityPredicates.NOT_MOUNTED
			);

			int id = 0;
			for (ArmorStandEntity armorStand : armorStands) {
				texturedArmorStands.put(armorStand, id);
				Iterable<ItemStack> equippedItems = InventoryUtils.getArmorFromEntity(armorStand);

				ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Text.literal("Head texture #" + id + ": ")));

				for (ItemStack stack : equippedItems) {
					ItemUtils.getHeadTextureOptional(stack).ifPresent(texture -> ctx.getSource().sendFeedback(Text.of(texture)));
				}
				id++;
			}
			return Command.SINGLE_SUCCESS;
		});
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpSoundCommand() {
		return ClientCommandManager.literal("dumpSound").executes(ctx -> {
			dumpSound = !dumpSound;
			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Text.literal("Dump sound: " + dumpSound)));
			return Command.SINGLE_SUCCESS;
		});
	}

	private LiteralArgumentBuilder<FabricClientCommandSource> dumpMayorCommand() {
		return ClientCommandManager.literal("dumpMayor").executes(ctx -> {
			String str = CaribouStonks.core().getHypixelDataSource().getElection() != null
					? CaribouStonks.core().getHypixelDataSource().getElection().toString()
					: "NULL";
			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Text.literal("--")));
			ctx.getSource().sendFeedback(Text.literal(str));
			ctx.getSource().sendFeedback(CaribouStonks.prefix().get().append(Text.literal("--")));
			return Command.SINGLE_SUCCESS;
		});
	}
}
