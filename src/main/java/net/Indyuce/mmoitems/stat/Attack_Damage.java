package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.mmogroup.mmolib.api.item.ItemTag;

public class Attack_Damage extends AttributeStat {
	public Attack_Damage() {
		super(new ItemStack(Material.IRON_SWORD), "Attack Damage", new String[] { "The amount of damage", "your weapon deals." }, "attack-damage", Attribute.GENERIC_ATTACK_DAMAGE, 1);
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		double value = ((DoubleData) data).generateNewValue();
		item.addItemTag(new ItemTag("MMOITEMS_ATTACK_DAMAGE", value));
		item.getLore().insert("attack-damage", format(value, "#", new StatFormat("##").format(value)));
		return true;
	}
}
