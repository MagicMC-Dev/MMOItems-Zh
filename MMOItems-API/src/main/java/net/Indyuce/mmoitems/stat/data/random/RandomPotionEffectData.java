package net.Indyuce.mmoitems.stat.data.random;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;

public class RandomPotionEffectData {
	private final PotionEffectType type;
	private final NumericStatFormula duration, amplifier;

	public RandomPotionEffectData(ConfigurationSection config) {
		Validate.notNull(config, "Potion effect config cannot be null");

		type = PotionEffectType.getByName(config.getName().toUpperCase().replace("-", "_").replace(" ", "_"));
		Validate.notNull(type, "Could not find potion effect with name '" + config.getName() + "'");

		duration = new NumericStatFormula(config.get("duration"));
		amplifier = new NumericStatFormula(config.get("amplifier"));
	}

	public RandomPotionEffectData(PotionEffectType type, NumericStatFormula amplifier) {
		this(type, new NumericStatFormula((double) MMOUtils.getEffectDuration(type) / 20d, 0, 0, 0), amplifier);
	}

	public RandomPotionEffectData(PotionEffectType type, NumericStatFormula duration, NumericStatFormula amplifier) {
		this.type = type;
		this.duration = duration;
		this.amplifier = amplifier;
	}

	public PotionEffectType getType() {
		return type;
	}

	public NumericStatFormula getDuration() {
		return duration;
	}

	public NumericStatFormula getAmplifier() {
		return amplifier;
	}

	public PotionEffectData randomize(MMOItemBuilder builder) {
		return new PotionEffectData(type, duration.calculate(builder.getLevel()), (int) amplifier.calculate(builder.getLevel()));
	}
}
