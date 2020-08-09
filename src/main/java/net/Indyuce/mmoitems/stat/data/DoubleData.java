package net.Indyuce.mmoitems.stat.data;

import java.util.Random;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class DoubleData implements StatData, Mergeable {
	private double min, max;

	private static final Random random = new Random();

	public DoubleData(Object object) {
		if (object instanceof Number) {
			min = Double.valueOf(object.toString());
			return;
		}

		if (object instanceof String) {
			String[] split = ((String) object).split("\\=");
			Validate.isTrue(split.length == 2, "Must specify a valid range");
			min = Double.parseDouble(split[0]);
			max = Double.parseDouble(split[1]);
			return;
		}

		throw new IllegalArgumentException("Must specify a range or a number");
	}

	public DoubleData(double min, double max) {
		this.min = min;
		this.max = max;
	}

	public boolean hasMax() {
		return max > min && max != 0;
	}

	public double getMin() {
		return min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double value) {
		max = value;
	}

	public void setMin(double value) {
		min = value;
	}

	public void add(double value) {
		min = min + value;
	}

	public void addRelative(double value) {
		min *= 1 + value;
	}

	public double generateNewValue() {
		return hasMax() ? min + random.nextDouble() * (max - min) : min;
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof DoubleData, "Cannot merge two different stat data types");
		min += ((DoubleData) data).min;
		// TODO if hasMax()
	}
}