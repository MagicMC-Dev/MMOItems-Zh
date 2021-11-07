package net.Indyuce.mmoitems.api.player.inventory;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;

public class EquippedPlayerItem {
	private final VolatileMMOItem item;
	private final EquipmentSlot slot;

	/**
	 * An item equipped by a player in a specific slot
	 * 
	 * @param item The item equipped
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
}
