package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.attribute.Attribute;
import org.jetbrains.annotations.NotNull;

public class Armor extends AttributeStat {
	public Armor() {
		super("ARMOR", VersionMaterial.GOLDEN_CHESTPLATE.toMaterial(), "Armor", new String[] { "The armor given to the holder." },
				Attribute.GENERIC_ARMOR);
	}
}
