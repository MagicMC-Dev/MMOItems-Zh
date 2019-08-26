package net.Indyuce.mmoitems.stat;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.version.VersionMaterial;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Soulbound_Level extends DoubleStat {
	public Soulbound_Level() {
		super(new ItemStack(VersionMaterial.ENDER_EYE.toMaterial()), "Soulbinding Level", new String[] { "The soulbound level defines how much", "damage players will take when trying", "to use a soulbound item. It also determines", "how hard it is to break the binding." }, "soulbound-level", new String[] { "consumable" });
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
