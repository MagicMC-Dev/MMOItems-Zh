package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Gem_Color extends StringStat {
	public Gem_Color() {
		super("GEM_COLOR", VersionMaterial.LIGHT_BLUE_DYE.toItem(), "Gem Color", new String[] { "Defines the color of the socket in", "which the gem can be applied." }, new String[] { "gem_stone" });
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		item.addItemTag(new ItemTag(getNBTPath(), data.toString()));
		return true;
	}
}
