package net.Indyuce.mmoitems.api.item.mmoitem;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;

public class LiveMMOItem extends ReadMMOItem {

	/**
	 * This class is used to load ALL the data from an item in one constructor.
	 * They should be used with care because it is quite performance heavy
	 * 
	 * @param item
	 *            The item to read
	 */
	public LiveMMOItem(ItemStack item) {
		this(MMOLib.plugin.getVersion().getWrapper().getNBTItem(item));
	}

	/**
	 * This class is used to load ALL the data from an item in one constructor.
	 * They should be used with care because it is quite performance heavy
	 * 
	 * @param item
	 *            The item to read
	 */
	public LiveMMOItem(NBTItem item) {
		super(item);

		// Reads all the stats that this type could possibly have.
		for (ItemStat stat : getType().getAvailableStats())

			// Attempts to load it
			try {

				// Will not do much if the stat is missing from the item
				stat.whenLoaded(this);

			// Some unknown error happened. L
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING,
						ChatColor.GRAY + "Could not load stat '"
							+ ChatColor.GOLD + stat.getId() + ChatColor.GRAY + "'item data from '"
							+ ChatColor.RED + getId() + ChatColor.GRAY + "': "
							+ ChatColor.YELLOW + exception.getMessage());
			}
	}
}
