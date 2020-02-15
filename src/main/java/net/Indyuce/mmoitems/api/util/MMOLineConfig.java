package net.Indyuce.mmoitems.api.util;

import org.apache.commons.lang.Validate;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

public class MMOLineConfig {
	private final String key, value;
	private final String[] args;
	private final JsonObject json;

	public MMOLineConfig(String value) {
		this.value = value;

		/*
		 * if there is no config, no need to parse the json object. split,
		 * define key and find arg
		 */
		if (!value.contains("{") || !value.contains("}")) {
			String[] split = value.split("\\ ");
			key = split[0];
			args = split.length > 1 ? value.replace(key + " ", "").split("\\ ") : new String[0];
			json = new JsonObject();
			return;
		}

		/*
		 * load json and extra args
		 */
		try {
			int begin = value.indexOf("{"), end = value.lastIndexOf("}") + 1;
			key = value.substring(0, begin);

			json = new JsonParser().parse(value.substring(begin, end)).getAsJsonObject();

			String format = value.substring(Math.min(value.length(), end + 1));
			args = format.isEmpty() ? new String[0] : format.split("\\ ");
		} catch (JsonParseException exception) {
			throw new IllegalArgumentException("Could not load config");
		}
	}

	/*
	 * extra arguments outside the config brackets
	 */
	public String[] args() {
		return args;
	}

	public String getKey() {
		return key;
	}

	public String getString(String path) {
		return json.get(path).getAsString();
	}

	public String getString(String path, String def) {
		return json.has(path) ? getString(path) : def;
	}

	public double getDouble(String path) {
		return json.get(path).getAsDouble();
	}

	public int getInt(String path) {
		return json.get(path).getAsInt();
	}

	public int getInt(String path, int def) {
		return json.has(path) ? getInt(path) : def;
	}

	public long getLong(String path) {
		return json.get(path).getAsLong();
	}

	public boolean getBoolean(String path) {
		return json.get(path).getAsBoolean();
	}

	public boolean getBoolean(String path, boolean def) {
		return json.has(path) ? getBoolean(path) : def;
	}

	public boolean contains(String path) {
		return json.has(path);
	}

	public void validate(String... paths) {
		for (String path : paths)
			Validate.isTrue(contains(path), "Config is missing parameter '" + path + "'");
	}

	public void validateArgs(int count) {
		Validate.isTrue(args.length >= count, "Config must have at least " + count + " parameters");
	}

	@Override
	public String toString() {
		return value;
	}
}
