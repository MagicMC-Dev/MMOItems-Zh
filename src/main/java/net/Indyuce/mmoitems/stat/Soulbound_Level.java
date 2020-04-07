package net.Indyuce.mmoitems.stat;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Soulbound_Level extends DoubleStat {
	public Soulbound_Level() {
		super("SOULBOUND_LEVEL", new ItemStack(VersionMaterial.ENDER_EYE.toMaterial()), "Soulbinding Level", new String[] { "The soulbound level defines how much", "damage players will take when trying", "to use a soulbound item. It also determines", "how hard it is to break the binding." }, new String[] { "consumable" });
	}

	// writes soulbound level with roman writing in lore
	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		int value = (int) ((DoubleData) data).generateNewValue();
		item.addItemTag(new ItemTag("MMOITEMS_SOULBOUND_LEVEL", value));
		item.getLore().insert("soulbound-level", format(value, "#", MMOUtils.intToRoman(value)));
		return true;
	}
}
