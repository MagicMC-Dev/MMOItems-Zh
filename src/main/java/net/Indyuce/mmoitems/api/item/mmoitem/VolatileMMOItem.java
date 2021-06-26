package net.Indyuce.mmoitems.api.item.mmoitem;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class VolatileMMOItem extends ReadMMOItem {

	/**
	 * VolatileMMOItems only loads the item data it needs instantly. The item
	 * data is only read when using hasData(ItemStat) for the first time.
	 * LiveMMOItems read everything on the constructor. VolativeMMOItems are
	 * used in player inventory updates.
	 * <p></p>
	 * Basically, use this to <b>quickly read Stat Data values</b> from an ItemStack.
	 * <p></p>
	 * If you are editing the stats, and then building a new item stack,
	 * you must use {@link LiveMMOItem}.
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
	public boolean hasData(@NotNull ItemStat stat) {
		if (!super.hasData(stat))

			// Attempt to lad this stat data
			try {
				stat.whenLoaded(this);

				// History not prematurely loaded?
				if (getStatHistory(stat) == null) {

					// Also load history :think ing:
					ItemTag hisTag = ItemTag.getTagAtPath(ItemStackBuilder.history_keyword + stat.getId(), getNBT(), SupportedNBTTagValues.STRING);

					if (hisTag != null) {
						// Aye
						StatHistory hist =  StatHistory.fromNBTString(this, (String) hisTag.getValue());

						// History valid? Record
						if (hist != null) { this.setStatHistory(stat, hist); }
					}
				}

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
