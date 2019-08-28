package net.Indyuce.mmoitems.api.util;

import java.util.HashMap;
import java.util.Map;

public class StringValue {
	private final String name;
	private final double value, extra;

	public StringValue(String name, double value) {
		this(name, value, -1);
	}

	public StringValue(String name, double value, double extra) {
		this.name = name;
		this.value = value;
		this.extra = extra;
	}

	public String getName() {
		return name;
	}

	public double getValue() {
		return value;
	}

	public boolean hasExtraValue() {
		return extra != -1;
	}

	public double getExtraValue() {
		return extra;
	}

	@Deprecated
	public static Map<String, Double> readFromArray(StringValue... array) {
		Map<String, Double> map = new HashMap<String, Double>();
		for (StringValue mod : array)
			map.put(mod.getName(), mod.getValue());
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof StringValue))
			return false;

		StringValue couple = (StringValue) obj;
		return couple.name == name && couple.value == value && couple.extra == extra;
	}
}
