package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.AttackWeaponStat;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.attribute.Attribute;

public class AttackSpeed extends AttackWeaponStat {
	public AttackSpeed() {
		super("ATTACK_SPEED", VersionMaterial.LIGHT_GRAY_DYE.toMaterial(), "Attack Speed",
				new String[] { "The speed at which your weapon strikes.", "In attacks/sec." }, Attribute.GENERIC_ATTACK_SPEED);
	}
}
