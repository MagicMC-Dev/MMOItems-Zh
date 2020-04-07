package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.api.itemgen.GeneratedItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class StringData implements StatData, RandomStatData {
	private String value;

	public StringData(String str) {
		this.value = str;
	}

	public void setString(String str) {
		this.value = str;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public StatData randomize(GeneratedItemBuilder builder) {
		return this;
	}
}