package net.Indyuce.mmoitems.api.player.inventory;

import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.Type.EquipmentSlot;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;

public class EquippedPlayerItem {
	private final VolatileMMOItem item;
	private final EquipmentSlot slot;

	/**
	 * An item equipped by a player in a specific slot
	 * 
	 * @param item The item equipped
	 * @param slot The corresponding MMOItems slot type, must not be null!
	 */
	public EquippedPlayerItem(EquippedItem item) {
		this.item = new VolatileMMOItem(item.getItem());
		this.slot = item.getSlot();
	}

	public VolatileMMOItem getItem() {
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
