package net.Indyuce.mmoitems.stat;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.version.VersionMaterial;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Required_Level extends DoubleStat {
	public Required_Level() {
		super(new ItemStack(VersionMaterial.EXPERIENCE_BOTTLE.toMaterial()), "Required Level", new String[] { "The level your item needs", "in order to be used." }, "required-level", new String[] { "all" });
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		int lvl = (int) ((DoubleData) data).generateNewValue();

		item.addItemTag(new ItemTag("MMOITEMS_REQUIRED_LEVEL", lvl));
		item.getLore().insert("required-level", format(lvl, "#", "" + lvl));
		return true;
	}
}
