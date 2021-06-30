package net.Indyuce.mmoitems.api.item.mmoitem;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.Type;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public abstract class ReadMMOItem extends MMOItem {
	@NotNull private final NBTItem item;

	/**
	 * This class is used when reading an MMOItem from an ItemStack (the
	 * opposite of ItemStackBuilder, like an ItemStackReader)
	 * 
	 * @param item
	 *            The NBTItem being read to generate an MMOItem
	 */
	public ReadMMOItem(@NotNull NBTItem item) {
		super(Type.get(item.getType()), item.getString("MMOITEMS_ITEM_ID"));

		this.item = item;
	}

	@Override
	public int getDamage() {

		// Does it use custom durability?
		if (hasData(ItemStats.MAX_DURABILITY)) {

			// Use the correct class
			DurabilityItem dItem = new DurabilityItem(null, getNBT());

			int max = dItem.getMaxDurability();
			int current = dItem.getDurability();

			// Difference
			return max - current;

		// Its using vanilla durability-yo
		} else {

			// Uh use the item stack I guess
			ItemStack asStack = getNBT().getItem();

			// Bruh
			if (asStack.hasItemMeta()) {
				ItemMeta iMeta = asStack.getItemMeta();

				// Is it damageable?
				if (iMeta instanceof Damageable) {

					// Return that damage
					return ((Damageable) iMeta).getDamage();
				}
			}

			// Not damageable no damage
			return 0;
		}
	}

	@NotNull public NBTItem getNBT() {
		return item;
	}
}
