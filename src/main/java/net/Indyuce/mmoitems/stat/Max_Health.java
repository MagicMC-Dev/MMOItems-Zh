package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Max_Health extends AttributeStat {
	public Max_Health() {
		super(new ItemStack(Material.GOLDEN_APPLE), "Max Health", new String[] { "The amount of health your", "item gives to the holder." }, "max-health", Attribute.GENERIC_MAX_HEALTH);
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		double value = ((DoubleData) data).generateNewValue();
		item.addItemTag(new ItemTag("MMOITEMS_MAX_HEALTH", value));
		item.getLore().insert("max-health", format(value, "#", new StatFormat("##").format(value)));
		return true;
	}
}
