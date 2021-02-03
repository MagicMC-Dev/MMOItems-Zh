package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import io.lumine.mythic.lib.api.item.ItemTag;

public class PickaxePower extends DoubleStat {
	public PickaxePower() {
		super("PICKAXE_POWER", Material.IRON_PICKAXE, "Pickaxe Power",
				new String[] { "The breaking strength of the", "item when mining custom blocks." }, new String[] { "tool" });
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		int pickPower = (int) ((DoubleData) data).getValue();

		item.addItemTag(new ItemTag("MMOITEMS_PICKAXE_POWER", pickPower));
		item.getLore().insert("pickaxe-power", formatNumericStat(pickPower, "#", "" + pickPower));
	}
}
