package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.type.AttributeStat;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.attribute.Attribute;

public class Armor extends AttributeStat {
	public Armor() {
		super("ARMOR", VersionMaterial.GOLDEN_CHESTPLATE.toMaterial(), "Armor", new String[] { "The armor given to the holder." },
				Attribute.GENERIC_ARMOR);
	}
}
