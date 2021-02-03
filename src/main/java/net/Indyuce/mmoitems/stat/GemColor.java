package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.version.VersionMaterial;

public class GemColor extends StringStat {
	public GemColor() {
		super("GEM_COLOR", VersionMaterial.LIGHT_BLUE_DYE.toMaterial(), "Gem Color", new String[] { "Defines the color of the socket in", "which the gem can be applied." }, new String[] { "gem_stone" });
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		item.addItemTag(new ItemTag(getNBTPath(), data.toString()));
	}
}
