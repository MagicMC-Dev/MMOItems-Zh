package net.Indyuce.mmoitems.api.player.inventory;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.Type.EquipmentSlot;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;

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
		this(MMOLib.plugin.getVersion().getWrapper().getNBTItem(item), slot);
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

	public boolean matches(Type type) {
		return slot == EquipmentSlot.ANY || (type.getEquipmentType() == EquipmentSlot.BOTH_HANDS ? slot.isHand()
				: slot == EquipmentSlot.BOTH_HANDS ? type.getEquipmentType().isHand() : slot == type.getEquipmentType());
	}
}