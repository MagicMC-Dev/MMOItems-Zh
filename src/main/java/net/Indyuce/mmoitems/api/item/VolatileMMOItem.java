package net.Indyuce.mmoitems.api.item;

import java.util.logging.Level;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.NBTItem;

public class VolatileMMOItem extends ReadMMOItem {

	/*
	 * using VolatileMMOItem compared to LiveMMOItem, MMOItems only loads the item
	 * data it really NEEDS. the item data is only read when using hasData(ItemStat)
	 * for the first time, whereas LiveMMOItem reads everything on constructor ; it
	 * is purely a performance concern
	 */
	public VolatileMMOItem(NBTItem item) {
		super(item);
	}

	/*
	 * since it checks for the data every time this method is called, this method
	 * must only be called ONCE if we want the best performance
	 */
	@Override
	public boolean hasData(ItemStat stat) {
		if (!super.hasData(stat))
			try {
				stat.whenLoaded(this);
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load stat '" + stat.getId()
						+ "'item data from '" + getId() + "': " + exception.getMessage());
			}
		return super.hasData(stat);
	}
}
