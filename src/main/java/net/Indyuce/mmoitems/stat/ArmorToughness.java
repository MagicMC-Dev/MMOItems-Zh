package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class ArmorToughness extends AttributeStat {
	public ArmorToughness() {
		super("ARMOR_TOUGHNESS", new ItemStack(Material.DIAMOND_CHESTPLATE), "Armor Toughness",
				new String[] { "Armor toughness reduces damage taken." }, Attribute.GENERIC_ARMOR_TOUGHNESS);
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		double value = ((DoubleData) data).getValue();
		item.addItemTag(new ItemTag("MMOITEMS_ARMOR_TOUGHNESS", value));
		item.getLore().insert("armor-toughness", formatNumericStat(value, "#", new StatFormat("##").format(value)));
	}
}
