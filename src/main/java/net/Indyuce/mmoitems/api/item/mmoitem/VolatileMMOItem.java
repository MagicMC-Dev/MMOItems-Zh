package net.Indyuce.mmoitems.api.item.mmoitem;

import java.util.logging.Level;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.NBTItem;

public class VolatileMMOItem extends ReadMMOItem {

	/**
	 * VolatileMMOItems only loads the item data it needs instantly. The item
	 * data is only read when using hasData(ItemStat) for the first time.
	 * LiveMMOItems read everything on the constructor. VolativeMMOItems are
	 * used in player inventory updates
	 * 
	 * @param item
	 *            The item to read from
	 */
	public VolatileMMOItem(NBTItem item) {
		super(item);
	}

	/**
	 * This should only be used once if we want the best performance. This
	 * method both checks for stat data, and loads it if it did found one
	 * 
	 * @return If the item has some stat data
	 */
	@Override
	public boolean hasData(ItemStat stat) {
		if (!super.hasData(stat))
			try {
				stat.whenLoaded(this);
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING,
						"Could not load stat '" + stat.getId() + "'item data from '" + getId() + "': " + exception.getMessage());
			}
		return super.hasData(stat);
	}
}
