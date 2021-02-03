package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import io.lumine.mythic.lib.api.item.ItemTag;

public class MaxHealth extends AttributeStat {
	public MaxHealth() {
		super("MAX_HEALTH", Material.GOLDEN_APPLE, "Max Health",
				new String[] { "The amount of health your", "item gives to the holder." }, Attribute.GENERIC_MAX_HEALTH);
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		double value = ((DoubleData) data).getValue();
		item.addItemTag(new ItemTag("MMOITEMS_MAX_HEALTH", value));
		item.getLore().insert("max-health", formatNumericStat(value, "#", new StatFormat("##").format(value)));
	}
}
