package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class Pickaxe_Power extends DoubleStat {
	public Pickaxe_Power() {
		super("PICKAXE_POWER", new ItemStack(Material.IRON_PICKAXE), "Pickaxe Power", new String[] { "The breaking strength of the", "item when mining custom blocks." }, new String[] { "tool" });
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		int pickPower = (int) ((DoubleData) data).generateNewValue();

		item.addItemTag(new ItemTag("MMOITEMS_PICKAXE_POWER", pickPower));
		item.getLore().insert("pickaxe-power", format(pickPower, "#", "" + pickPower));
		return true;
	}
}
