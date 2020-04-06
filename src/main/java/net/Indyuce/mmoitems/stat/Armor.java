package net.Indyuce.mmoitems.stat;

import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Armor extends AttributeStat {
	public Armor() {
		super("ARMOR", new ItemStack(VersionMaterial.GOLDEN_CHESTPLATE.toMaterial()), "Armor", new String[] { "The armor given to the holder." }, Attribute.GENERIC_ARMOR);
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		double value = ((DoubleData) data).generateNewValue();
		// for (String slot : item.getMMOItem().getType().getSlots())
		// item.addItemAttribute(new Attribute("armor", value, slot));
		item.addItemTag(new ItemTag("MMOITEMS_ARMOR", value));
		item.getLore().insert("armor", format(value, "#", new StatFormat("##").format(value)));
		return true;
	}
}
