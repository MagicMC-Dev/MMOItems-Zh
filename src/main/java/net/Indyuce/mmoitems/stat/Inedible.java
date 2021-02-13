package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import org.jetbrains.annotations.NotNull;

public class Inedible extends BooleanStat {
	public Inedible() {
		super("INEDIBLE", Material.POISONOUS_POTATO, "Inedible", new String[] { "Players won't be able to", "right-click this consumable." }, new String[] { "consumable" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
		if (((BooleanData) data).isEnabled())
			item.addItemTag(new ItemTag("MMOITEMS_INEDIBLE", true));
	}
}
