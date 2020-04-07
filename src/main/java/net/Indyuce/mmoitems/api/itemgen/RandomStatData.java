package net.Indyuce.mmoitems.api.itemgen;

import net.Indyuce.mmoitems.stat.data.type.StatData;

public interface RandomStatData {

	/*
	 * generate a real stat data based on the item builder which contains
	 * information about the item level.
	 */
	public StatData randomize(GeneratedItemBuilder builder);
}
