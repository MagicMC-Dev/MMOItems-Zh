package net.Indyuce.mmoitems.stat.data;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;

public class DoubleData implements StatData, Mergeable {
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
	public void merge(StatData data) {
		Validate.isTrue(data instanceof DoubleData, "Cannot merge two different stat data types");
		value += ((DoubleData) data).value;
	}

	@Override
	public @NotNull StatData cloneData() { return new DoubleData(getValue()); }
}