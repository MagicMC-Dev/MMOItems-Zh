package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.AttackWeaponStat;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;

public class AttackSpeed extends AttackWeaponStat {
	public AttackSpeed() {
		super("ATTACK_SPEED", Material.LIGHT_GRAY_DYE, "攻击速度",
				new String[] { "武器的攻击速度 攻击/秒" }, Attribute.GENERIC_ATTACK_SPEED);
	}
}
