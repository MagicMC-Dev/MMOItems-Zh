package net.Indyuce.mmoitems.api;

import org.bukkit.configuration.ConfigurationSection;

public class ReforgeOptions {
	private final boolean keepName, keepLore, keepEnchantments, keepModifications, keepSoulbind;

	public ReforgeOptions(ConfigurationSection config) {
		this.keepName = config.getBoolean("display-name");
		this.keepLore = config.getBoolean("lore");
		this.keepEnchantments = config.getBoolean("enchantments");
		this.keepModifications = config.getBoolean("modifications");
		this.keepSoulbind = config.getBoolean("soulbound");
	}

	public boolean shouldKeepName() {
		return keepName;
	}

	public boolean shouldKeepLore() {
		return keepLore;
	}

	public boolean shouldKeepEnchantments() {
		return keepEnchantments;
	}

	public boolean shouldKeepModifications() {
		return keepModifications;
	}

	public boolean shouldKeepSoulbind() {
		return keepSoulbind;
	}
}
