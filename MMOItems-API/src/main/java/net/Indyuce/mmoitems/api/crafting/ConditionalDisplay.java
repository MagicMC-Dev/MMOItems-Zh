package net.Indyuce.mmoitems.api.crafting;

import org.bukkit.configuration.ConfigurationSection;

public class ConditionalDisplay {
	private final String positive, negative;

	public ConditionalDisplay(String positive, String negative) {
		this.positive = positive;
		this.negative = negative;
	}

	/**
	 * Used when loading translations
	 */
	public ConditionalDisplay(ConfigurationSection config) {
		this(config.getString("positive"), config.getString("negative"));
	}

	public String format(boolean positive) {
		return positive ? this.positive : negative;
	}

	public void setup(ConfigurationSection config) {
		config.set("positive", positive);
		config.set("negative", negative);
	}
}
