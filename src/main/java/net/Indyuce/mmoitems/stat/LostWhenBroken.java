package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;

public class LostWhenBroken extends BooleanStat {
	public LostWhenBroken() {
		super("WILL_BREAK", Material.SHEARS, "Lost when Broken?", new String[] { "If set to true, the item will be lost", "once it reaches 0 durability." }, new String[] { "!block", "all" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
		if (((BooleanData) data).isEnabled())
			item.addItemTag(new ItemTag("MMOITEMS_WILL_BREAK", true));
	}
}
