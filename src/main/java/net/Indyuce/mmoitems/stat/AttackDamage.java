package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

import net.Indyuce.mmoitems.stat.type.AttributeStat;

public class AttackDamage extends AttributeStat {
	public AttackDamage() {
		super("ATTACK_DAMAGE", Material.IRON_SWORD, "Attack Damage", new String[] { "The amount of damage", "your weapon deals." },
				Attribute.GENERIC_ATTACK_DAMAGE, 1);
	}
}
