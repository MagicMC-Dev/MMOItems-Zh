package net.Indyuce.mmoitems.api.crafting;

import org.bukkit.configuration.ConfigurationSection;

public class ConditionalDisplay {
	private final String positive, negative;

	public ConditionalDisplay(String positive, String negative) {
		this.positive = positive;
		this.negative = negative;
	}

	/*
	 * used when loading translations
	 */
	public ConditionalDisplay(ConfigurationSection config) {
		this(config.getString("positive"), config.getString("negative"));
	}

	public String getPositive() {
		return positive;
	}

	public String getNegative() {
		return negative;
	}

	public void setup(ConfigurationSection config) {
		config.set("positive", positive);
		config.set("negative", negative);
	}
}
