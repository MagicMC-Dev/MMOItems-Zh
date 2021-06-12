package net.Indyuce.mmoitems.stat.type;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class DisableStat extends BooleanStat {
	public DisableStat(String id, Material material, String name, String... lore) {
		super("DISABLE_" + id, material, name, lore, new String[] { "all" });
	}

	public DisableStat(String id, Material material, String name, Material[] materials, String... lore) {
		super("DISABLE_" + id, material, name, lore, new String[] { "all" }, materials);
	}

	public DisableStat(String id, Material material, String name, String[] types, String... lore) {
		super("DISABLE_" + id, material, name, lore, types);
	}

	@Override
	public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
		if (((BooleanData) data).isEnabled())
			item.addItemTag(new ItemTag("MMOITEMS_" + getId(), true));
	}
}
