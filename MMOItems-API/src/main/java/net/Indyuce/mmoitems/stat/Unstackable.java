package net.Indyuce.mmoitems.stat;

import java.util.UUID;

import org.bukkit.Material;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import org.jetbrains.annotations.NotNull;

public class Unstackable extends BooleanStat {
	public Unstackable() {
		super("UNSTACKABLE", Material.CHEST_MINECART, "Unstackable",
				new String[] { "This will make the item unable", "to be stacked with itself." }, new String[] { "all" });
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull BooleanData data) {
		if (data.isEnabled()) {
			item.addItemTag(new ItemTag(getNBTPath(), true));
			item.addItemTag(new ItemTag(getNBTPath() + "_UUID", UUID.randomUUID().toString()));
		}
	}
}
