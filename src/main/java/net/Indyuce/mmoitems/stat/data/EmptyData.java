package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class EmptyData implements RandomStatData {
	@Override
	public StatData randomize(MMOItemBuilder builder) {
		return null;
	}

	@Override
	public boolean isPresent() {
		return false;
	}
}
