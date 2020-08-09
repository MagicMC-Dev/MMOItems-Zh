package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.ProperStat;

public class LegacyDurability extends DoubleStat implements ProperStat {
	public LegacyDurability() {
		super("DURABILITY", new ItemStack(Material.FISHING_ROD), "Item Damage/ID", new String[] { "The durability/ID of your item. This", "does &cNOT&7 impact the item max durability." }, new String[] { "all" });
	}

	@Override
	@SuppressWarnings("deprecation")
	public void whenApplied(ItemStackBuilder item, StatData data) {
		item.getItemStack().setDurability((short) ((DoubleData) data).generateNewValue());
	}

	@Override
	@SuppressWarnings("deprecation")
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().getItem().getDurability() > 0)
			mmoitem.setData(ItemStat.DURABILITY, new DoubleData(mmoitem.getNBT().getItem().getDurability()));
	}
}
