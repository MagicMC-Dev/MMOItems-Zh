package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.stat.data.type.StatData;

public class BooleanData implements StatData {
	private final boolean state;

	public BooleanData(boolean state) {
		this.state = state;
	}

	public boolean isEnabled() {
		return state;
	}

	@Override
	public boolean isEmpty() {
		return !state;
	}

	@Override
	public String toString() {
		return "" + state;
	}
}
