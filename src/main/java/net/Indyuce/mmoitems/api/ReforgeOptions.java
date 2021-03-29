package net.Indyuce.mmoitems.api;

import org.bukkit.configuration.ConfigurationSection;

public class ReforgeOptions {
	private final boolean
			keepName,
			keepLore,
			keepEnchantments,
			keepUpgrades,
			keepGemStones,
			keepSoulbind,
			keepExternalSH;

	public ReforgeOptions(ConfigurationSection config) {
		this.keepName = config.getBoolean("display-name");
		this.keepLore = config.getBoolean("lore");
		this.keepEnchantments = config.getBoolean("enchantments");
		this.keepUpgrades = config.getBoolean("upgrades");
		this.keepGemStones = config.getBoolean("gemstones");
		this.keepSoulbind = config.getBoolean("soulbound");
		this.keepExternalSH = config.getBoolean("external-sh", true);
	}

	/**
	 * Keeps the display name of the item.
	 */
	public boolean shouldKeepName() {
		return keepName;
	}

	/**
	 *  Keeps all lore lines that begin with {@link org.bukkit.ChatColor#GRAY}
	 */
	public boolean shouldKeepLore() {
		return keepLore;
	}

	/**
	 * Should this keep the enchantments the player
	 * manually cast onto this item? (Not from gem
	 * stones nor upgrades).
	 */
	public boolean shouldKeepEnchantments() {
		return keepEnchantments;
	}

	/**
	 * Keep 'extraneous' data registered onto the Stat History
	 */
	public boolean shouldKeepExternalSH() {
		return keepExternalSH;
	}

	/**
	 * Retains the upgrade level of the item.
	 */
	public boolean shouldKeepUpgrades() {
		return keepUpgrades;
	}

	/**
	 * Retains all gem stones if there are any, removing
	 * one gem socket for every gemstone kept.
	 * <p></p>
	 * Gemstones remember at what upgrade level they were inserted.
	 */
	public boolean shouldKeepGemStones() {
		return keepGemStones;
	}

	/**
	 * Retains the soulbind if it has any.
	 */
	public boolean shouldKeepSoulbind() {
		return keepSoulbind;
	}
}
