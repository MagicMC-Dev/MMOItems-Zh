package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.jetbrains.annotations.NotNull;

public class GemColor extends StringStat implements GemStoneStat {
	public GemColor() {
		super("GEM_COLOR", VersionMaterial.LIGHT_BLUE_DYE.toMaterial(), "Gem Color", new String[] { "Defines the color of the socket in", "which the gem can be applied." }, new String[] { "gem_stone" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
		item.addItemTag(new ItemTag(getNBTPath(), data.toString()));
	}
}
