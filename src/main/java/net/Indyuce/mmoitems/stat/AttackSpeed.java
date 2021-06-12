package net.Indyuce.mmoitems.stat;

import org.bukkit.attribute.Attribute;

import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.stat.type.AttributeStat;

public class AttackSpeed extends AttributeStat {
	public AttackSpeed() {
		super("ATTACK_SPEED", VersionMaterial.LIGHT_GRAY_DYE.toMaterial(), "Attack Speed",
				new String[] { "The speed at which your weapon strikes.", "In attacks/sec." }, Attribute.GENERIC_ATTACK_SPEED, 4);
	}
}
