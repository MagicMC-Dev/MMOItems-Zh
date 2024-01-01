package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class DoubleData implements StatData, Mergeable<DoubleData> {
	private double value;

	public DoubleData(double value) {
		this.value = value;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public void add(double extra) {
		value += extra;
	}

	public void addRelative(double coef) {
		value *= 1 + coef;
	}

	@Override
	public void mergeWith(DoubleData data) {
		value += data.value;
	}

	@Override
	public DoubleData clone() { return new DoubleData(value); }

	@Override
	public boolean isEmpty() {
		return value == 0;
	}

	@Override
	public String toString() { return String.valueOf(getValue()); }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DoubleData)) return false;
        return ((DoubleData) obj).getValue() == getValue();
    }
}