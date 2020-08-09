package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class Unbreakable extends BooleanStat {
	public Unbreakable() {
		super("UNBREAKABLE", new ItemStack(Material.ANVIL), "Unbreakable", new String[] { "Infinite durability if set to true." }, new String[] { "all" });
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled()) {
			item.addItemTag(new ItemTag("Unbreakable", true));
			item.getMeta().addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("Unbreakable"))
			mmoitem.setData(this, new BooleanData(mmoitem.getNBT().getBoolean("Unbreakable")));
	}
}
