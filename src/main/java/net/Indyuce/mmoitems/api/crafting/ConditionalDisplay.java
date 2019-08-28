package net.Indyuce.mmoitems.api.crafting;

import org.bukkit.configuration.ConfigurationSection;

public class ConditionalDisplay {
	private String negative;
	private String positive;

	public ConditionalDisplay(String positive, String negative) {
		this.positive = positive;
		this.negative = negative;
	}

	public String getPositive() {
		return positive;
	}

	public String getNegative() {
		return negative;
	}

	/*
	 * used when loading translations
	 */
	public void load(ConfigurationSection config) {
		positive = config.getString("positive");
		negative = config.getString("negative");
	}

	public void setup(ConfigurationSection config) {
		config.set("positive", positive);
		config.set("negative", negative);
	}
}
