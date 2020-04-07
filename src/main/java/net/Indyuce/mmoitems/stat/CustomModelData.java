package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ProperStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class CustomModelData extends DoubleStat implements ProperStat {
	public CustomModelData() {
		super("CUSTOM_MODEL_DATA", new ItemStack(Material.PAINTING), "Custom Model Data", new String[] { "Your 1.14+ model data." }, new String[] { "all" });

		if (MMOLib.plugin.getVersion().isBelowOrEqual(1, 13))
			disable();
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		item.addItemTag(new ItemTag("CustomModelData", (int) ((DoubleData) data).generateNewValue()));
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.hasTag("CustomModelData"))
			mmoitem.setData(this, new DoubleData(item.getDouble("CustomModelData")));
	}
}
