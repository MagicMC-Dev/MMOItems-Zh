package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringData implements StatData, RandomStatData, Mergeable {
	@Nullable private String value;

	public StringData(@Nullable String str) {
		this.value = str;
	}

	public void setString(@Nullable String str) {
		this.value = str;
	}
	@Nullable public String getString() { return value; }

	@Override
	public String toString() {
		return String.valueOf(value);
	}

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		return this;
	}

	@Override
	public void merge(@Nullable StatData data) {
		if (!(data instanceof StringData)) { return; }

		// Overwrite
		value = ((StringData) data).getString();
	}

	@NotNull
	@Override
	public StatData cloneData() {

		return new StringData(value);
	}

	@Override
	public boolean isClear() {

		// If empty I guess
		return value == null || value.isEmpty();
	}
}