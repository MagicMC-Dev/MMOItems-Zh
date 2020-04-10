package net.Indyuce.mmoitems.api.item;

import java.util.logging.Level;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;

public class LiveMMOItem extends MMOItem {
	private final NBTItem item;

	public LiveMMOItem(ItemStack item) {
		this(MMOLib.plugin.getNMS().getNBTItem(item));
	}

	/*
	 * loads all the item data of an item into an MMOItem instance.
	 */
	public LiveMMOItem(NBTItem item) {
		super(item.getType(), item.getString("MMOITEMS_ITEM_ID"));

		this.item = item;

		for (ItemStat stat : getType().getAvailableStats())
			try {
				stat.whenLoaded(this, item);
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING,
						"Could not load stat '" + stat.getId() + "'item data from '" + getId() + "': " + exception.getMessage());
			}
	}

	public NBTItem getItem() {
		return item;
	}
}
