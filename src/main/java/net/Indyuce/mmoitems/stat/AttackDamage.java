package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.AttributeStat;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

public class AttackDamage extends AttributeStat {
	public AttackDamage() {
		super("ATTACK_DAMAGE", Material.IRON_SWORD, "Attack Damage", new String[] { "The amount of damage", "your weapon deals." },
				Attribute.GENERIC_ATTACK_DAMAGE, 1);
	}
}
