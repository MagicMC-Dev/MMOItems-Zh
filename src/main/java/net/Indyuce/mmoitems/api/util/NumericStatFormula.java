package net.Indyuce.mmoitems.api.util;

import java.text.DecimalFormat;
import java.util.Random;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.api.item.template.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class NumericStatFormula implements RandomStatData {
	private final double base, scale, spread, maxSpread;

	private static final Random random = new Random();
	private static final DecimalFormat digit = new DecimalFormat("0.####");

	public static final NumericStatFormula ZERO = new NumericStatFormula(0, 0, 0, 0);

	/**
	 * When reading a numeric stat formula either from a config file
	 * (configuration section, or number) or when reading player input when a
	 * player edits a stat (string message). Although the string format would
	 * work in the config as well.
	 * 
	 * Throws an IAE either if the format is not good or if the object does not
	 * have the right type
	 * 
	 * @param object
	 *            Object to read data from.
	 */
	public NumericStatFormula(Object object) {
		Validate.notNull(object, "Config must not be null");

		if (object instanceof String) {
			String[] split = object.toString().split("\\ ");
			base = Double.parseDouble(split[0]);
			scale = Double.parseDouble(split[1]);
			spread = Double.parseDouble(split[2]);
			maxSpread = Double.parseDouble(split[3]);
			return;
		}

		if (object instanceof Number) {
			base = Double.valueOf(object.toString());
			scale = 0;
			spread = 0;
			maxSpread = 0;
			return;
		}

		if (object instanceof ConfigurationSection) {
			ConfigurationSection config = (ConfigurationSection) object;
			base = config.getDouble("base");
			scale = config.getDouble("scale");
			spread = config.getDouble("spread");
			maxSpread = config.getDouble("max-spread", .3);

			Validate.isTrue(spread >= 0, "Spread must be positive");
			Validate.isTrue(maxSpread >= 0, "Max spread must be positive and lower than 1");
			return;
		}

		throw new IllegalArgumentException("Must specify a config section, a string or a number");
	}

	/**
	 * Formula for numeric statistics. These formulas allow stats to scale
	 * accordingly to the item level but also have a more or less important
	 * gaussian based random factor
	 * 
	 * @param base
	 *            Base value
	 * @param scale
	 *            Value which scales with the item level
	 * @param spread
	 *            The relative standard deviation of a normal law centered on
	 *            (base + scale * level). If it's set to 0.1, the standard
	 *            deviation will be 10% of the stat value without the random
	 *            factor.
	 * @param maxSpread
	 *            The max amount of deviation you can have. If it's set to 0.3,
	 *            let A = base + scale * level, then the final stat value will
	 *            be in [0.7 * A, 1.3 * A]
	 */
	public NumericStatFormula(double base, double scale, double spread, double maxSpread) {
		this.base = base;
		this.scale = scale;
		this.spread = spread;
		this.maxSpread = maxSpread;
	}

	public double getBase() {
		return base;
	}

	public double getScale() {
		return scale;
	}

	public double getSpread() {
		return spread;
	}

	public double getMaxSpread() {
		return maxSpread;
	}

	public double calculate(double x) {
		return (base + scale * x) * (1 + Math.min(Math.max(random.nextGaussian() * spread, -maxSpread), maxSpread));
	}

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		return new DoubleData(calculate(builder.getLevel()));
	}

	/**
	 * Save some formula in a config file. This method is used when editing stat
	 * data in the edition GUI (when a player inputs a numeric formula)
	 * 
	 * @param config
	 *            The formula will be saved in that config file
	 * @param path
	 *            The config path used to save the formula
	 */
	public void fillConfigurationSection(ConfigurationSection config, String path) {
		if (scale == 0 && spread == 0 && maxSpread == 0)
			config.set(path, base == 0 ? null : base);
		else {
			config.set(path + ".base", base);
			config.set(path + ".scale", scale);
			config.set(path + ".spread", spread);
			config.set(path + ".max-spread", maxSpread);
		}
	}

	@Override
	public String toString() {

		if (scale == 0 && spread == 0)
			return digit.format(base);

		if (scale == 0)
			return "[" + digit.format(base * (1 - maxSpread)) + " -> " + digit.format(base * (1 + maxSpread)) + "] (" + digit.format(spread * 100)
					+ "% Spread) (" + digit.format(base) + " Avg)";

		return "{Base=" + digit.format(base) + (scale != 0 ? ",Scale=" + digit.format(scale) : "") + (spread != 0 ? ",Spread=" + spread : "")
				+ (maxSpread != 0 ? ",Max=" + maxSpread : "") + "}";
	}
}
