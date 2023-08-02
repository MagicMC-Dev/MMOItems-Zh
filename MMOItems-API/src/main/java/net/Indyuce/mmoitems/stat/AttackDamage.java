package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.AttackWeaponStat;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

public class AttackDamage extends AttackWeaponStat {
	public AttackDamage() {
		super("ATTACK_DAMAGE", Material.IRON_SWORD, "攻击伤害", new String[] { "你的武器造成的伤害量" },
				Attribute.GENERIC_ATTACK_DAMAGE);
	}
}
