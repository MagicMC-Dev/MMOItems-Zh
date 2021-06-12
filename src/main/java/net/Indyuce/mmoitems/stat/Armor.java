package net.Indyuce.mmoitems.stat;

import org.bukkit.attribute.Attribute;

import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.stat.type.AttributeStat;

public class Armor extends AttributeStat {
	public Armor() {
		super("ARMOR", VersionMaterial.GOLDEN_CHESTPLATE.toMaterial(), "Armor", new String[] { "The armor given to the holder." },
				Attribute.GENERIC_ARMOR);
	}
}
