package net.Indyuce.mmoitems.api.item.mmoitem;

import net.Indyuce.mmoitems.api.Type;
import io.lumine.mythic.lib.api.item.NBTItem;

public abstract class ReadMMOItem extends MMOItem {
	private final NBTItem item;

	/**
	 * This class is used when reading an MMOItem from an ItemStack (the
	 * opposite of ItemStackBuilder, like an ItemStackReader)
	 * 
	 * @param item
	 *            The NBTItem being read to generate an MMOItem
	 */
	public ReadMMOItem(NBTItem item) {
		super(Type.get(item.getType()), item.getString("MMOITEMS_ITEM_ID"));

		this.item = item;
	}

	public NBTItem getNBT() {
		return item;
	}
}
