package net.Indyuce.mmoitems.api;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public class ReforgeOptions {
	public static boolean dropRestoredGems;

	private final boolean
			keepName;
	private final boolean keepLore;
	private final boolean keepEnchantments;
	private final boolean keepUpgrades;
	private final boolean keepGemStones;
	private final boolean keepSoulbind;
	private final boolean keepExternalSH;

	public boolean isRegenerate() {
		return regenerate;
	}

	private final boolean regenerate;

	@NotNull String keepCase = ChatColor.GRAY.toString();
	public void  setKeepCase(@NotNull String kc) { keepCase = kc; }
	@NotNull public String getKeepCase() { return keepCase; }

	public ReforgeOptions(ConfigurationSection config) {
		this.keepName = config.getBoolean("display-name");
		this.keepLore = config.getBoolean("lore");
		this.keepEnchantments = config.getBoolean("enchantments");
		this.keepUpgrades = config.getBoolean("upgrades");
		this.keepGemStones = config.getBoolean("gemstones");
		this.keepSoulbind = config.getBoolean("soulbound");
		this.keepCase = config.getString("kept-lore-prefix", ChatColor.GRAY.toString());
		this.keepExternalSH = config.getBoolean("external-sh", true);
		this.regenerate = false;
	}

	public ReforgeOptions(boolean keepName, boolean keepLore, boolean keepEnchantments, boolean keepUpgrades, boolean keepGemStones, boolean keepSoulbind, boolean keepExternalSH, boolean regenerate) {
		this.keepName = keepName;
		this.keepLore = keepLore;
		this.keepEnchantments = keepEnchantments;
		this.keepUpgrades = keepUpgrades;
		this.keepGemStones = keepGemStones;
		this.keepSoulbind = keepSoulbind;
		this.keepExternalSH = keepExternalSH;
		this.regenerate = regenerate;
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
