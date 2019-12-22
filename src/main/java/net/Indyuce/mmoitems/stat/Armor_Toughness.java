package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class Armor_Toughness extends AttributeStat {
	public Armor_Toughness() {
		super(new ItemStack(Material.DIAMOND_CHESTPLATE), "Armor Toughness", new String[] { "Armor toughness reduces damage taken." }, "armor-toughness", Attribute.GENERIC_ARMOR_TOUGHNESS);
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		double value = ((DoubleData) data).generateNewValue();
		item.addItemTag(new ItemTag("MMOITEMS_ARMOR_TOUGHNESS", value));
		item.getLore().insert("armor-toughness", format(value, "#", new StatFormat("##").format(value)));
		return true;
	}
}
