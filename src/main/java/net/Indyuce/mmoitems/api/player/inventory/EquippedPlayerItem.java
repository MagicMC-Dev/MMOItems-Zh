package net.Indyuce.mmoitems.api.player.inventory;

import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;

public class EquippedPlayerItem {
	private final VolatileMMOItem item;
	private final EquipmentSlot slot;
	private final EquippedItem equipped;

	/**
	 * An item equipped by a player in a specific slot
	 * 
	 * @param item The item equipped
	 */
	public EquippedPlayerItem(EquippedItem item) {
		this.equipped = item;
		this.item = new VolatileMMOItem(item.getItem());
		this.slot = item.getSlot();
	}

	/**
	 * @return honestly I do not know why EquippedPlayerItem even exists?
	 * 		   you can get all the values from the {@link EquippedItem}
	 * 		   it came from. Its like a funny wrapper.
	 */
	public EquippedItem getEquipped() { return equipped; }

	public VolatileMMOItem getItem() { return item; }

	public EquipmentSlot getSlot() { return slot; }
}
