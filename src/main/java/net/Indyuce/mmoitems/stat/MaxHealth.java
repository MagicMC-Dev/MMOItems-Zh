package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class MaxHealth extends AttributeStat {
	public MaxHealth() {
		super("MAX_HEALTH", new ItemStack(Material.GOLDEN_APPLE), "Max Health", new String[] { "The amount of health your", "item gives to the holder." }, Attribute.GENERIC_MAX_HEALTH);
	}

	@Override
	public void whenApplied(MMOItemBuilder item, StatData data) {
		double value = ((DoubleData) data).generateNewValue();
		item.addItemTag(new ItemTag("MMOITEMS_MAX_HEALTH", value));
		item.getLore().insert("max-health", formatNumericStat(value, "#", new StatFormat("##").format(value)));
	}
}
