package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.ProperStat;
import net.mmogroup.mmolib.api.item.NBTItem;

public class DefaultDurability extends DoubleStat implements ProperStat {
	public DefaultDurability() {
		super("DURABILITY", new ItemStack(Material.FISHING_ROD), "Item Damage", new String[] { "Default item damage. This does &cNOT", "impact the item's max durability." }, new String[] { "all" });
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (item.getMeta() instanceof Damageable)
			((Damageable) item.getMeta()).setDamage((int) ((DoubleData) data).generateNewValue());
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.getItem().getItemMeta() instanceof Damageable)
			mmoitem.setData(ItemStat.DURABILITY, new DoubleData(((Damageable) item.getItem().getItemMeta()).getDamage()));
	}
}
