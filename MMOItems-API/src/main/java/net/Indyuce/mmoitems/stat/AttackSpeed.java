package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.AttackWeaponStat;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.attribute.Attribute;

public class AttackSpeed extends AttackWeaponStat {
	public AttackSpeed() {
		super("ATTACK_SPEED", VersionMaterial.LIGHT_GRAY_DYE.toMaterial(), "攻击速度",
				new String[] { "武器的攻击速度 攻击/秒" }, Attribute.GENERIC_ATTACK_SPEED);
	}
}
