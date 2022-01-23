package net.Indyuce.mmoitems.api.player.inventory;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.Type;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class EquippedItem {
	private final NBTItem item;
	private final EquipmentSlot slot;

	/**
	 * An item equipped by a player in a specific slot
	 * 
	 * @param item The item equipped
	 * @param slot The corresponding MMOItems slot type
	 */
	public EquippedItem(ItemStack item, EquipmentSlot slot) {
		this(NBTItem.get(item), slot);
	}

	/**
	 * An item equipped by a player in a specific slot
	 * 
	 * @param item The item equipped
	 * @param slot The corresponding MMOItems slot type, must not be null!
	 */
	public EquippedItem(NBTItem item, EquipmentSlot slot) {
		this.item = item;
		this.slot = slot;
	}

	public NBTItem getItem() {
		return item;
	}
	
	public EquipmentSlot getSlot() {
		return slot;
	}

	/**
	 * The slot this equipped item is defined to be, will this <code>Type</code>
	 * actually add its stats to the player when held here?
	 * <p></p>
	 * An <code>OFF_CATALYST</code> may only add in the <code>OFFHAND</code>, and such.
	 */
	public boolean matches(@NotNull Type type) {
		if (slot == EquipmentSlot.ANY)
			return true;

		if (type.getEquipmentType() == EquipmentSlot.BOTH_HANDS)
			return slot.isHand();

		return slot == type.getEquipmentType();
	}
}