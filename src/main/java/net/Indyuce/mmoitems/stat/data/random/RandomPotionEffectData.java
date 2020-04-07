package net.Indyuce.mmoitems.stat.data.random;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.itemgen.GeneratedItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;

public class RandomPotionEffectData {
	private final PotionEffectType type;
	private final NumericStatFormula duration, level;

	public RandomPotionEffectData(ConfigurationSection config) {
		Validate.notNull(config, "Potion effect config cannot be null");

		type = PotionEffectType.getByName(config.getName().toUpperCase().replace("-", "_").replace(" ", "_"));
		Validate.notNull(type, "Could not find potion effect with name '" + config.getName() + "'");

		duration = new NumericStatFormula(config.getConfigurationSection("duration"));
		level = new NumericStatFormula(config.getConfigurationSection("level"));
	}

	public RandomPotionEffectData(PotionEffectType type, NumericStatFormula level) {
		this(type, new NumericStatFormula((double) MMOUtils.getEffectDuration(type) / 20d, 0, 0, 0), level);
	}

	public RandomPotionEffectData(PotionEffectType type, NumericStatFormula duration, NumericStatFormula level) {
		this.type = type;
		this.duration = duration;
		this.level = level;
	}

	public PotionEffectType getType() {
		return type;
	}

	public NumericStatFormula getDuration() {
		return duration;
	}

	public NumericStatFormula getLevel() {
		return level;
	}

	public PotionEffectData randomize(GeneratedItemBuilder builder) {
		return new PotionEffectData(type, duration.calculate(builder.getLevel()), (int) level.calculate(builder.getLevel()));
	}
}
