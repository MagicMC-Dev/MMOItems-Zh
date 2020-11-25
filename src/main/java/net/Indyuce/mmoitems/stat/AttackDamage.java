package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

public class AttackDamage extends AttributeStat {
	public AttackDamage() {
		super("ATTACK_DAMAGE", Material.IRON_SWORD, "Attack Damage", new String[] { "The amount of damage", "your weapon deals." },
				Attribute.GENERIC_ATTACK_DAMAGE, 1);
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		double value = ((DoubleData) data).getValue();
		item.addItemTag(new ItemTag(getNBTPath(), value));
		item.getLore().insert(getPath(), formatNumericStat(value, "#", new StatFormat("##").format(value)));
	}
}
