package net.Indyuce.mmoitems.api.item.mmoitem;

import java.util.logging.Level;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.bukkit.ChatColor;

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

			// Attempt to lad this stat data
			try {
				stat.whenLoaded(this);

			// Nope
			} catch (IllegalArgumentException exception) {

				// Log a warning
				MMOItems.plugin.getLogger().log(Level.WARNING,
						ChatColor.GRAY + "Could not load stat '"
								+ ChatColor.GOLD + stat.getId() + ChatColor.GRAY + "'item data from '"
								+ ChatColor.RED + getId() + ChatColor.GRAY + "': "
								+ ChatColor.YELLOW + exception.getMessage());
			}
		return super.hasData(stat);
	}
}
