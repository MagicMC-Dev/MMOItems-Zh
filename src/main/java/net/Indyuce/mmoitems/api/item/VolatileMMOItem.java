package net.Indyuce.mmoitems.api.item;

import java.util.logging.Level;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.NBTItem;

public class VolatileMMOItem extends ReadMMOItem {

	/*
	 * instead of loading all the item data directly when the constructor is
	 * called, some systems do not require to know all the data from a specific
	 * item. therefore the data is only loaded when the stat is being checked on
	 * hasData()
	 */
	public VolatileMMOItem(NBTItem item) {
		super(item);
	}

	/*
	 * since it checks for the data every time this method is called, this
	 * method must only be used once if we want the best performance
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
