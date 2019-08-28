package net.Indyuce.mmoitems.api.interaction.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

public class DurabilityState {
	private String id;

	// how it is displayed in the lore. it is already color-formatted
	private String display;

	// use ratio = <uses-left> / <max-durability>
	private double minRatio, maxRatio;

	public DurabilityState(ConfigurationSection config) {
		this.minRatio = config.getDouble("use-ratio.min") / 100;
		this.maxRatio = config.getDouble("use-ratio.max") / 100;
		this.display = ChatColor.translateAlternateColorCodes('&', config.getString("lore-tag"));
		this.id = config.getName().toUpperCase().replace("-", "_");
	}

	public String getID() {
		return id;
	}

	public boolean isInState(double current, double max) {
		double ratio = current / max;
		return maxRatio >= ratio && ratio >= minRatio;
	}

	public String getDisplay() {
		return display;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof DurabilityState && ((DurabilityState) obj).id.equals(id);
	}
}
