package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;

public class CustomModelData extends DoubleStat implements GemStoneStat {
	public CustomModelData() {
		super("CUSTOM_MODEL_DATA", new ItemStack(Material.PAINTING), "Custom Model Data", new String[] { "Your 1.14+ model data." }, new String[] { "!block", "all" });

		if (MMOLib.plugin.getVersion().isBelowOrEqual(1, 13))
			disable();
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		item.addItemTag(new ItemTag("CustomModelData", (int) ((DoubleData) data).getValue()));
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().hasTag("CustomModelData"))
			mmoitem.setData(this, new DoubleData(mmoitem.getNBT().getDouble("CustomModelData")));
	}
}
