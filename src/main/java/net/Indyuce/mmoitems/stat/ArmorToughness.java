package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

public class ArmorToughness extends AttributeStat {
	public ArmorToughness() {
		super("ARMOR_TOUGHNESS", Material.DIAMOND_CHESTPLATE, "Armor Toughness",
				new String[] { "Armor toughness reduces damage taken." }, Attribute.GENERIC_ARMOR_TOUGHNESS);
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		double value = ((DoubleData) data).getValue();
		item.addItemTag(new ItemTag(getNBTPath(), value));
		item.getLore().insert(getPath(), formatNumericStat(value, "#", new StatFormat("##").format(value)));
	}
}
