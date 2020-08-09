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

public class AttackDamage extends AttributeStat {
	public AttackDamage() {
		super("ATTACK_DAMAGE", new ItemStack(Material.IRON_SWORD), "Attack Damage", new String[] { "The amount of damage", "your weapon deals." }, Attribute.GENERIC_ATTACK_DAMAGE, 1);
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		double value = ((DoubleData) data).generateNewValue();
		item.addItemTag(new ItemTag("MMOITEMS_ATTACK_DAMAGE", value));
		item.getLore().insert("attack-damage", formatNumericStat(value, "#", new StatFormat("##").format(value)));
	}
}
