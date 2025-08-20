package fr.siroz.cariboustonks.util;

import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

/**
 * Utility class providing methods for interacting with inventories
 */
public final class InventoryUtils {

	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	private InventoryUtils() {
	}

	/**
	 * Retrieves the currently held item of the client.
	 *
	 * @return the {@link ItemStack} representing the item currently held by the client or {@code null}
	 */
	@Nullable
	public static ItemStack getHeldItem() {
		return CLIENT.player != null ? CLIENT.player.getInventory().getSelectedStack() : null;
	}

	/**
	 * Determines if the currently held item has the specified SkyBlock item ID.
	 *
	 * @param skyBlockItemId the SkyBlock item ID to compare against the held item's ID
	 * @return {@code true} if the currently held is not null, and the skyBlockItemId matches
	 */
	public static boolean isHoldingItem(@NotNull String skyBlockItemId) {
		ItemStack held = getHeldItem();
		return held != null && ItemUtils.getSkyBlockItemId(held).equals(skyBlockItemId);
	}

	/**
	 * Retrieves the {@code helmet} currently equipped by the client.
	 *
	 * @return the {@link ItemStack} representing the helmet equipped by the client or {@code null}
	 */
	@Nullable
	public static ItemStack getHelmet() {
		return CLIENT.player != null ? CLIENT.player.getEquippedStack(EquipmentSlot.HEAD) : null;
	}

	/**
	 * Retrieves a list of armor items currently equipped by the given {@link LivingEntity}.
	 *
	 * @param entity the living entity
	 * @return list of {@link ItemStack} representing the armor items equipped by the entity
	 */
	@NotNull
	@Unmodifiable
	public static List<ItemStack> getArmorFromEntity(@NotNull LivingEntity entity) {
		return AttributeModifierSlot.ARMOR.getSlots().stream()
				.filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
				.map(entity::getEquippedStack)
				.toList();
	}

	/**
	 * Converts a hotbar index into the corresponding slot index in the player's inventory.
	 *
	 * @param hotbarIndex the index of the hotbar (0-8 inclusive)
	 * @return the slot index in the player's inventory corresponding to the given hotbar index,
	 * or {@code -1} if the provided hotbar index is out of the valid range
	 */
	public static int convertHotbarToSlotIndex(int hotbarIndex) {
		if (hotbarIndex < 0 || hotbarIndex > 8) return -1;

		return 36 + hotbarIndex;
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
}
