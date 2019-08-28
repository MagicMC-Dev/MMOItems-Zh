package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Custom_Model_Data extends DoubleStat {
	public Custom_Model_Data() {
		super(new ItemStack(Material.PAINTING), "Custom Model Data", new String[] { "Your 1.14 model data." }, "custom-model-data", new String[] { "all" });

		if (MMOItems.plugin.getVersion().isBelowOrEqual(1, 13))
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
