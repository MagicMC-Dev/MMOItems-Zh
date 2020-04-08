package net.Indyuce.mmoitems.api.itemgen.tier;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.itemgen.NumericStatFormula;

public class RandomTierInfo {
	private final ItemTier tier;
	private final double chance;
	private final NumericStatFormula capacity;

	public RandomTierInfo(ConfigurationSection config) {
		Validate.notNull(config, "Config cannot be null");

		String tierFormat = config.getName().toUpperCase().replace("-", "_").replace(" ", "_");
		Validate.isTrue(MMOItems.plugin.getTiers().has(tierFormat), "Could not find tier with ID '" + tierFormat + "'");
		tier = MMOItems.plugin.getTiers().get(tierFormat);

		chance = config.getDouble("chance");
		Validate.isTrue(chance > 0 && chance < 1, "Chance must be stricly between 0 and 1");

		capacity = new NumericStatFormula(config.get("capacity"));
	}

	/*
	 * constructor for the default tier: there is no need for chance or tier
	 * instance
	 */
	public RandomTierInfo(NumericStatFormula capacity) {
		this(null, 0, capacity);
	}

	public RandomTierInfo(ItemTier tier, double chance, NumericStatFormula capacity) {
		this.tier = tier;
		this.chance = chance;
		this.capacity = capacity;
	}

	public boolean isDefault() {
		return tier == null;
	}

	public ItemTier getTier() {
		return tier;
	}

	public NumericStatFormula getCapacity() {
		return capacity;
	}

	public double getChance() {
		return chance;
	}

	public RolledTier roll(int itemLevel) {
		return new RolledTier(this, itemLevel);
	}
}