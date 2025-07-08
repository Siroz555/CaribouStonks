package fr.siroz.cariboustonks.util;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.JsonOps;
import fr.siroz.cariboustonks.CaribouStonks;
import fr.siroz.cariboustonks.core.json.GsonProvider;
import fr.siroz.cariboustonks.core.scheduler.TickScheduler;
import fr.siroz.cariboustonks.util.colors.Color;
import fr.siroz.cariboustonks.util.colors.Colors;
import fr.siroz.cariboustonks.util.render.Texture;
import fr.siroz.cariboustonks.util.render.WorldRenderUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.GameVersion;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class DeveloperTools {

	private static final boolean SYSTEM_DEBUG = Boolean.parseBoolean(System.getProperty("stonks.debug", "false"));

	private static final Object2IntOpenHashMap<ArmorStandEntity> ARMORSTANDS_TEXTURED = new Object2IntOpenHashMap<>();
	private static final RegistryWrapper.WrapperLookup LOOKUP = BuiltinRegistries.createWrapperLookup();

	private DeveloperTools() {
	}

	public static void initDeveloperTools() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, _ra) -> dispatcher.register(
				ClientCommandManager.literal(CaribouStonks.NAMESPACE)
						.then(ClientCommandManager.literal("devtools")
								.then(dumpHeldItemSimpleCommand())
								.then(dumpHeldItemCommand())
								.then(dumpArmorStandHeadTextures()))
		));

		WorldRenderEvents.AFTER_TRANSLUCENT.register(DeveloperTools::render);

		if (isInDevelopment()) {
			GameVersion version = SharedConstants.getGameVersion();
			CaribouStonks.LOGGER.warn("Debug mode enabled ({}) {}", version.getName(), DeveloperTools.isSnapshot() ? "(Snapshot)" : "");

			ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
				if (client.player != null && DeveloperTools.isInDevelopment()) {
					Text snapShot = DeveloperTools.isSnapshot()
							? Text.literal(" (Snapshot)").formatted(Formatting.DARK_RED) : Text.empty();
					client.player.sendMessage(CaribouStonks.prefix().get()
							.append(Text.literal("Debug mode enabled (" + version.getName() + ")").formatted(Formatting.RED)
									.append(snapShot)), false);
				}
			});
			WorldRenderEvents.AFTER_TRANSLUCENT.register(DeveloperTools::debugRender);
		}
	}

	public static boolean isInDevelopment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment()
				|| !SharedConstants.getGameVersion().isStable()
				|| SYSTEM_DEBUG;
	}

	public static boolean isSnapshot() {
		return !SharedConstants.getGameVersion().isStable();
	}

	public static RegistryWrapper.WrapperLookup getRegistryLookup() {
		MinecraftClient client = MinecraftClient.getInstance();
		return client != null && client.getNetworkHandler() != null && client.getNetworkHandler().getRegistryManager() != null
				? client.getNetworkHandler().getRegistryManager()
				: LOOKUP;
	}

	private static void render(WorldRenderContext context) {
		if (!ARMORSTANDS_TEXTURED.isEmpty()) {
			for (Object2IntMap.Entry<ArmorStandEntity> armorStand : ARMORSTANDS_TEXTURED.object2IntEntrySet()) {
				ArmorStandEntity entity = armorStand.getKey();
				if (entity == null || entity.getPos() == null) {
					continue;
				}

				Vec3d centerPos = entity.getPos();
				WorldRenderUtils.renderText(context, Text.literal("#" + armorStand.getIntValue()),
						centerPos.add(0, 1, 0), 1, true);
			}
		}
	}

	private static void debugRender(WorldRenderContext context) {
		WorldRenderUtils.renderText(context,
				Text.of("CaribouStonks " + SharedConstants.getGameVersion().getName()),
				new Vec3d(-1.5, 69, 25.5), 1.3f,
				true);

		WorldRenderUtils.renderFilledWithBeaconBeam(context,
				new BlockPos(1, 70, 25),
				Colors.RED,
				.5f,
				true);

		WorldRenderUtils.renderCircle(context,
				new Vec3d(5, 65, 18),
				5,
				16,
				.02f,
				Colors.YELLOW,
				Direction.Axis.Y,
				false);

		WorldRenderUtils.renderThickCircle(context,
				new Vec3d(5, 63, 24),
				5,
				2,
				64,
				Colors.AQUA.withAlpha(0.5f),
				false);

		WorldRenderUtils.renderLinesFromPoints(context,
				new Vec3d[]{
						new Vec3d(0, 68, 17),
						new Vec3d(4, 70, 20),
						new Vec3d(5, 71, 17)},
				Colors.MAGENTA,
				5f,
				true);

		WorldRenderUtils.renderQuad(context, new Vec3d[]{
						new Vec3d(4, 66, 29.5),
						new Vec3d(4, 66, 28.5),
						new Vec3d(4, 68, 28.5),
						new Vec3d(4, 68, 29.5)},
				Colors.YELLOW,
				.5f,
				true);

		Vec3d centerPos = new Vec3d(3, 66, 18);
		double distance = context.camera().getPos().distanceTo(centerPos);
		float scale = Math.max((float) distance / 10, 1);

		WorldRenderUtils.renderTexture(
				context,
				centerPos,
				scale,
				scale,
				1f,
				1f,
				new Vec3d(0, 0, 0),
				Texture.NETHERITE_SWORD,
				new Color(255, 255, 255),
				1f,
				true
		);
	}

	// ----------------- DUMP COMMANDS -----------------

	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemSimpleCommand() {
		return ClientCommandManager.literal("dumpHeldItemSimple").executes(context -> {

			ItemStack item = context.getSource().getPlayer().getMainHandStack();
			if (item == null || item.isEmpty()) {
				context.getSource().sendFeedback(CaribouStonks.prefix().get()
						.append(Text.literal("Item is null or empty").formatted(Formatting.RED)));
			} else {

				MutableText message = Text.empty();
				message.append(item.getName()).append(" :")
						.append("\n")
						.append(" - SkyBlockItemId: " + ItemUtils.getSkyBlockItemId(item)).formatted(Formatting.GRAY)
						.append("\n")
						.append(" - SkyBlockApiId: " + ItemUtils.getSkyBlockApiId(item)).formatted(Formatting.GRAY);

				context.getSource().sendFeedback(CaribouStonks.prefix().get().append(message));
			}

			return Command.SINGLE_SUCCESS;
		});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpHeldItemCommand() {
		return ClientCommandManager.literal("dumpHeldItem").executes(context -> {
			String json = GsonProvider.standard().toJson(ItemStack.CODEC.encodeStart(
					getRegistryLookup().getOps(JsonOps.INSTANCE),
					context.getSource().getPlayer().getMainHandStack()).getOrThrow());

			context.getSource().sendFeedback(CaribouStonks.prefix().get()
					.append(Text.literal("Debug Held Item: " + json)));

			return Command.SINGLE_SUCCESS;
		});
	}

	private static LiteralArgumentBuilder<FabricClientCommandSource> dumpArmorStandHeadTextures() {
		return ClientCommandManager.literal("dumpArmorStandHeadTextures").executes(context -> {

			ARMORSTANDS_TEXTURED.clear();
			TickScheduler.getInstance().runLater(ARMORSTANDS_TEXTURED::clear, 20 * 10);

			List<ArmorStandEntity> armorStands = context.getSource().getWorld().getEntitiesByClass(
					ArmorStandEntity.class,
					context.getSource().getPlayer().getBoundingBox().expand(8d),
					EntityPredicates.NOT_MOUNTED);

			int id = 0;
			for (ArmorStandEntity armorStand : armorStands) {
				ARMORSTANDS_TEXTURED.put(armorStand, id);
				Iterable<ItemStack> equippedItems = ItemUtils.getArmor(armorStand);

				context.getSource().sendFeedback(CaribouStonks.prefix().get()
						.append(Text.literal("Head texture #" + id + ": ")));

				for (ItemStack stack : equippedItems) {
					ItemUtils.getHeadTextureOptional(stack)
							.ifPresent(texture -> context.getSource().sendFeedback(Text.of(texture)));
				}

				id++;
			}

			return Command.SINGLE_SUCCESS;
		});
	}
}
