package net.Indyuce.mmoitems.api.util;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class StringValue {
	private final String name;
	private final double value;

	@Deprecated
	public StringValue(String name, double value) {
		this.name = name;
		this.value = value;
	}

	@Deprecated
	public String getName() {
		return name;
	}

	@Deprecated
	public double getValue() {
		return value;
	}

	@Deprecated
	public static Map<String, Double> readFromArray(StringValue... array) {
		Map<String, Double> map = new HashMap<>();
		for (StringValue mod : array)
			map.put(mod.getName(), mod.getValue());
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof StringValue && ((StringValue) obj).name.equals(name) && ((StringValue) obj).value == value;
	}
}
