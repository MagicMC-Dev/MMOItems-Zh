package net.Indyuce.mmoitems.api.item.mmoitem;

import java.util.logging.Level;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;

public class LiveMMOItem extends ReadMMOItem {
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

		for (ItemStat stat : getType().getAvailableStats())
			try {
				stat.whenLoaded(this);
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING,
						"Could not load stat '" + stat.getId() + "'item data from '" + getId() + "': " + exception.getMessage());
			}
	}
}
