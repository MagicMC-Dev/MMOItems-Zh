package net.Indyuce.mmoitems.stat.data.random;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.RestoreData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class RandomRestoreData implements RandomStatData<RestoreData> {
	private final NumericStatFormula health, food, saturation;

	public RandomRestoreData(ConfigurationSection config) {
		Validate.notNull(config, "Could not load restore config");

		health = config.contains("health") ? new NumericStatFormula(config) : NumericStatFormula.ZERO;
		food = config.contains("food") ? new NumericStatFormula(config) : NumericStatFormula.ZERO;
		saturation = config.contains("saturation") ? new NumericStatFormula(config) : NumericStatFormula.ZERO;
	}

	public NumericStatFormula getHealth() {
		return health;
	}

	public NumericStatFormula getFood() {
		return food;
	}

	public NumericStatFormula getSaturation() {
		return saturation;
	}

	@Override
	public RestoreData randomize(MMOItemBuilder builder) {
		return new RestoreData(health.calculate(builder.getLevel()), food.calculate(builder.getLevel()), saturation.calculate(builder.getLevel()));
	}
}