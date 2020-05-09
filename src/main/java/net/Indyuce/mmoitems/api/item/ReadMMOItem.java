package net.Indyuce.mmoitems.api.item;

import net.mmogroup.mmolib.api.item.NBTItem;

public abstract class ReadMMOItem extends MMOItem {
	private final NBTItem item;

	public ReadMMOItem(NBTItem item) {
		super(item.getType(), item.getString("MMOITEMS_ITEM_ID"));

		this.item = item;
	}

	public NBTItem getNBT() {
		return item;
	}
}
