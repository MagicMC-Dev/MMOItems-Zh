package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.attribute.Attribute;

public class AttackSpeed extends AttributeStat {
	public AttackSpeed() {
		super("ATTACK_SPEED", VersionMaterial.LIGHT_GRAY_DYE.toMaterial(), "Attack Speed",
				new String[] { "The speed at which your weapon strikes.", "In attacks/sec." }, Attribute.GENERIC_ATTACK_SPEED, 4);
	}
}
