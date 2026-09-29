package fr.siroz.cariboustonks.util;

import com.mojang.serialization.Codec;
import fr.siroz.cariboustonks.core.infrastructure.json.GsonProvider;
import fr.siroz.cariboustonks.platform.context.PlayerContext;
import it.unimi.dsi.fastutil.chars.CharList;
import java.awt.Color;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Position;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class MinecraftUtil {
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	public static final Pattern COLOR_CODE_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]"); // §[0-9a-fklmnor]
	private static final CharList FORMAT_CODES = CharList.of('4', 'c', '6', 'e', '2', 'a', 'b', '3', '1', '9', 'd', '5', 'f', '7', '8', '0', 'r', 'k', 'l', 'm', 'n', 'o');
	public static final Codec<Color> COLOR_CODEC = Codec.INT.xmap(argb -> new Color(argb, true), Color::getRGB);

	private MinecraftUtil() {
	}

	/**
	 * Checks if a {@link Player} as {@code Entity} is a real player, and not an enemy or NPC
	 *
	 * @return {@code true} if is a real player
	 */
	public static boolean isPlayer(@NonNull Entity entity) {
		return entity instanceof Player player && player.getUUID().version() == 4;
	}

	/**
	 * Strip legacy formatting from the given input
	 *
	 * @param input the input String
	 * @return the input without formatting codes or an empty String if the input is null or empty
	 */
	public static @NonNull String stripColor(@Nullable String input) {
		if (input == null || input.isEmpty()) return "";

		return COLOR_CODE_PATTERN.matcher(input).replaceAll("");
	}

	/**
	 * Retrieve the {@code Player Head Texture} from the given player username
	 *
	 * @param playerName the player username
	 * @return the Identifier from the Client Assets
	 */
	public static @NonNull Identifier getPlayerHeadTexture(@Nullable String playerName) {
		if (playerName == null || MINECRAFT.getConnection() == null) return DefaultPlayerSkin.getDefaultTexture();

		PlayerInfo entry = MINECRAFT.getConnection().getPlayerInfo(playerName);
		if (entry == null) return DefaultPlayerSkin.getDefaultTexture();

		return entry.getSkin().body().id();
	}

	/**
	 * Retrieve the {@code Sound Name} of the given Sound Packet.
	 * <p>
	 * {@code entity.warden.death}
	 *
	 * @param soundPacket the sound packet
	 * @return the sound name of an empty String
	 */
	public static @NonNull String convertSoundPacketToName(@Nullable ClientboundSoundPacket soundPacket) {
		if (soundPacket == null) return "";
		return soundPacket.getSound().value().location().getPath();
	}

	/**
	 * Calculates the squared distance between two positions, ignoring their Y coordinates.
	 *
	 * @param from the starting position
	 * @param to   the destination position
	 * @return the squared distance between the two positions
	 */
	public static double squaredDistanceToIgnoringY(@NonNull Position from, @NonNull Position to) {
		double dx = from.x() - to.x();
		double dz = from.z() - to.z();
		return dx * dx + dz * dz;
	}

	/**
	 * Returns the {@link Component} formatted for the given {@link PlayerInfo}.
	 * <p>
	 * Import depuis PlayerTabOverlay
	 *
	 * @param playerInfo  the player
	 * @param profileName the profile name
	 * @return the Component
	 */
	public static Component getNameForDisplay(@NonNull PlayerInfo playerInfo, @NonNull String profileName) {
		return playerInfo.getTabListDisplayName() != null
				? playerInfo.getTabListDisplayName().copy()
				: PlayerTeam.formatNameForTeam(playerInfo.getTeam(), Component.literal(profileName));
	}

	/**
	 * Retrieves a list of armor items currently equipped by the given {@link LivingEntity}.
	 *
	 * @param entity the living entity
	 * @return list of {@link ItemStack} representing the armor items equipped by the entity
	 */
	@NonNull
	public static List<ItemStack> getArmorFromEntity(@NonNull LivingEntity entity) {
		return EquipmentSlotGroup.ARMOR.slots().stream()
				.filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
				.map(entity::getItemBySlot)
				.toList();
	}

	/**
	 * Show Special Effet
	 *
	 * @param item        item
	 * @param particle    particle
	 * @param particleAge particle age between 1-120
	 * @param sound       sound
	 * @param soundVolume sound volume
	 * @param soundPitch  sound pitch
	 */
	public static void showSpecialEffect(
			@NonNull ItemStack item,
			@Nullable ParticleOptions particle,
			int particleAge,
			@Nullable SoundEvent sound,
			float soundVolume,
			float soundPitch
	) {
		showSpecialEffect(null, item, particle, particleAge, sound, soundVolume, soundPitch);
	}

	/**
	 * Show Special Effet
	 *
	 * @param title       title
	 * @param item        item
	 * @param particle    particle
	 * @param particleAge particle age between 1-120
	 * @param sound       sound
	 * @param soundVolume sound volume
	 * @param soundPitch  sound pitch
	 */
	public static void showSpecialEffect(
			@Nullable Component title,
			@NonNull ItemStack item,
			@Nullable ParticleOptions particle,
			int particleAge,
			@Nullable SoundEvent sound,
			float soundVolume,
			float soundPitch
	) {
		if (MINECRAFT.player != null && MINECRAFT.level != null) {
			if (title != null) {
				PlayerContext.showTitle(title, 0, 60, 20);
			}

			MINECRAFT.player.displayItemActivation(item);

			if (particle != null) {
				MINECRAFT.particleEngine.createTrackingEmitter(MINECRAFT.player, particle, particleAge);
			}

			if (sound != null) {
				MINECRAFT.player.playSound(sound, soundVolume, soundPitch);
			}
		}
	}

	public static Optional<String> textToJson(@NonNull Component text) {
		try {
			String json = GsonProvider.standard().toJson(text);
			return Optional.of(json);
		} catch (Exception _) {
			return Optional.empty();
		}
	}

	public static Optional<Component> jsonToText(@NonNull String json) {
		try {
			Component text = GsonProvider.standard().fromJson(json, Component.class);
			return Optional.ofNullable(text);
		} catch (Exception _) {
			return Optional.empty();
		}
	}

	public static @NonNull Style findStyle(@NonNull Component component) {
		return component.getSiblings().isEmpty()
				? component.getStyle()
				: component.getSiblings().getLast().getStyle();
	}

	public static MutableComponent formatTextFromLegacy(@NonNull String legacy) {
		if (legacy.contains("§")) return formatTextFromLegacy(legacy, '§');
		if (legacy.contains("&")) return formatTextFromLegacy(legacy, '&');
		return Component.literal(legacy);
	}

	/**
	 * Checks if a given slot in an inventory is located at the edge.
	 *
	 * @param slotId the slot ID to check, where the slots are numbered sequentially from 0
	 * @param rows   the total number of rows in the inventory
	 * @return {@code true} if the slot is on the edge of the inventory (first or last column, or first or last row)
	 */
	public static boolean isEdgeSlot(int slotId, int rows) {
		if (slotId < 0 || slotId >= rows * 9) return false;
		int row = slotId / 9;
		int col = slotId % 9;
		return col == 0 || col == 8 || row == 0 || row == rows - 1;
	}

	/**
	 * Converts a hotbar index into the corresponding slot index in the player's inventory.
	 *
	 * @param hotbarIndex the index of the hotbar (0-8 inclusive)
	 * @return the slot index in the player's inventory corresponding to the given hotbar index,
	 * or {@code -1} if the provided hotbar index is out of the valid range
	 */
	@SuppressWarnings("unused")
	public static int convertHotbarToSlotIndex(int hotbarIndex) {
		if (hotbarIndex < 0 || hotbarIndex > 8) return -1;

		return 36 + hotbarIndex;
	}

	/**
	 * Vérifie si le client est connecté à Hypixel.
	 *
	 * @return {@code true}/ {@code false}
	 */
	public static boolean isConnectedToHypixel() {
		String serverAddress = MINECRAFT.getCurrentServer() != null
				? MINECRAFT.getCurrentServer().ip.toLowerCase(Locale.ENGLISH)
				: "";
		String serverBrand = MINECRAFT.player != null && MINECRAFT.player.connection.serverBrand() != null
				? MINECRAFT.player.connection.serverBrand()
				: "";

		if (serverBrand == null) {
			return false;
		}

		return serverAddress.contains("hypixel.net") || serverBrand.contains("Hypixel BungeeCord");
	}

	private static @NonNull MutableComponent formatTextFromLegacy(@NonNull String legacy, char legacyPrefix) {
		MutableComponent newText = Component.empty();
		StringBuilder builder = new StringBuilder();
		ChatFormatting formatting = null;
		Boolean bold = null;
		Boolean italic = null;
		Boolean underline = null;
		Boolean strikethrough = null;
		Boolean obfuscated = null;

		for (int i = 0; i < legacy.length(); i++) {
			if (i != 0 && legacy.charAt(i - 1) == legacyPrefix
					&& FORMAT_CODES.contains(Character.toLowerCase(legacy.charAt(i))) && !builder.isEmpty()
			) {
				newText.append(Component.literal(builder.toString())
						.setStyle(Style.EMPTY
								.withColor(formatting)
								.withBold(bold)
								.withItalic(italic)
								.withUnderlined(underline)
								.withStrikethrough(strikethrough)
								.withObfuscated(obfuscated)));

				builder.delete(0, builder.length());
				formatting = null;
				bold = null;
				italic = null;
				underline = null;
				strikethrough = null;
				obfuscated = null;
			}

			if (i != 0 && legacy.charAt(i - 1) == legacyPrefix) {
				ChatFormatting byCode = ChatFormatting.getByCode(legacy.charAt(i));
				switch (byCode) {
					case BOLD -> bold = true;
					case ITALIC -> italic = true;
					case UNDERLINE -> underline = true;
					case STRIKETHROUGH -> strikethrough = true;
					case OBFUSCATED -> obfuscated = true;

					case null, default -> formatting = byCode;
				}
				continue;
			}

			if (legacy.charAt(i) != legacyPrefix && (i == 0 || legacy.charAt(i - 1) != legacyPrefix)) {
				builder.append(legacy.charAt(i));
			}

			if (i == legacy.length() - 1) {
				newText.append(Component.literal(builder.toString())
						.setStyle(Style.EMPTY
								.withColor(formatting)
								.withBold(bold)
								.withItalic(italic)
								.withUnderlined(underline)
								.withStrikethrough(strikethrough)
								.withObfuscated(obfuscated)));
			}
		}
		return newText;
	}
}
