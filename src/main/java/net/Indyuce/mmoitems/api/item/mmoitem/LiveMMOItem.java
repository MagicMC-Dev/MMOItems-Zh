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

	/*
	 * loads all the item data of an item into an MMOItem instance.
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
