package net.Indyuce.mmoitems.api.util;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.random.UpdatableRandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Random;

/**
 * That Gaussian spread distribution thing that no one understands.
 *
 * @author indyuce
 */
public class NumericStatFormula implements RandomStatData, UpdatableRandomStatData {
	private final double base, scale, spread, maxSpread;

	private static final Random RANDOM = new Random();
	private static final DecimalFormat DIGIT = new DecimalFormat("0.####");

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
	 * @param object Object to read data from.
	 */
	public NumericStatFormula(Object object) {
		Validate.notNull(object, "Config must not be null");

		if (object instanceof String) {
			String[] split = object.toString().split(" ");
			base = Double.parseDouble(split[0]);
			scale = split.length > 1 ? Double.parseDouble(split[1]) : 0;
			spread = split.length > 2 ? Double.parseDouble(split[2]) : 0;
			maxSpread = split.length > 3 ? Double.parseDouble(split[3]) : 0;
			return;
		}

		if (object instanceof Number) {
			base = Double.parseDouble(object.toString());
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
			Validate.isTrue(maxSpread >= 0, "Max spread must be positive");
			return;
		}

		throw new IllegalArgumentException("Must specify a config section, a string or a number");
	}

	/**
	 * Formula for numeric statistics. These formulas allow stats to scale
	 * accordingly to the item level but also have a more or less important
	 * gaussian based random factor
	 * 
	 * @param base      Base value
	 * @param scale     Value which scales with the item level
	 * @param spread    The relative standard deviation of a normal law centered
	 *                  on (base + scale * level). If it's set to 0.1, the
	 *                  standard deviation will be 10% of the stat value without
	 *                  the random factor.
	 * @param maxSpread The max amount of deviation you can have. If it's set to
	 *                  0.3, let A = base + scale * level, then the final stat
	 *                  value will be in [0.7 * A, 1.3 * A]
	 */
	public NumericStatFormula(double base, double scale, double spread, double maxSpread) {
		this.base = base;
		this.scale = scale;
		this.spread = spread;
		this.maxSpread = maxSpread;
	}

	/**
	 * @return The 'Base' number of the item. This chooses the peak of the Gaussian Distribution.
	 * @see #getScale()
	 */
	public double getBase() { return base; }


	/**
	 * @return When the item has a certain level or tier, this is how much each level shifts
	 * 		   the peak, so that it is centered at {@code base + scale*level}
	 * @see #getBase()
	 */
	public double getScale() { return scale; }

	/**
	 * @return Standard Deviation of the Gaussian Distribution
	 */
	public double getSpread() { return spread; }

	/**
	 * @return For gaussian distributions, there always is that INSANELY SMALL chance of getting an INSANELY LARGE number.
	 * 		   For example: At base atk dmg 10, and standard deviation 1:
	 * 		   <p>68% of rolls will fall between 9 and 11;
	 * 		   </p>95% of rolls will fall between 8 and 12;
	 * 		   <p>99.7% of rolls will fall between 7 and 13;
	 * 		   </p>10E-42 of a roll that will give you an epic 300 dmg sword
	 * 		   <p></p>
	 * 		   Whatever, this constrains to a minimum and maximum of output.
	 */
	public double getMaxSpread() { return maxSpread; }

	public static boolean useRelativeSpread;

	/**
	 * Applies the formula for a given input x.
	 * 
	 * @param  levelScalingFactor When choosing the mean of the distribution,
	 *                            the formula is <code>base + (scale*level)</code>.
	 *                            This is the <code>level</code>
	 *
	 * @return   <b>Legacy formula: ???</b><br>
	 * 			 Let A = {base} + {scale} * lvl, then the returned value is a
	 *           random value taken in respect to a gaussian distribution
	 *           centered on A, with average spread of {spread}%, and with a
	 *           maximum offset of {maxSpread}% (relative to average value)
	 *           <p></p>
	 *           <b>Formula: Spread = Standard Deviation</b>
	 *           The mean, the peak is located at <code>{base} + {scale}*lvl</code>. <br>
	 *           The 'spread' is the standard deviation of the distribution. <br>
	 *           'Max Spread' constrains the result of this operation at <code>{mean}±{max spread}</code>
	 */
	public double calculate(double levelScalingFactor) {

		// Calculate yes
		return calculate(levelScalingFactor, RANDOM.nextGaussian());
	}

	/**
	 * @param levelScalingFactor Level to scale the scale with
	 * @param random Result of <code>RANDOM.nextGaussian()</code> or whatever other
	 *               value that you actually want to pass.
	 *
	 * @return The calculated value
	 */
	public double calculate(double levelScalingFactor, double random) {

		if (useRelativeSpread) {
			//SPRD//if (spread > 0) MMOItems.log("\u00a7c༺\u00a77 Using \u00a7eRelative\u00a77 spread formula: \u00a76μ=" + (base + scale * levelScalingFactor) + "\u00a77, \u00a73σ=" + (spread * (base + scale * levelScalingFactor) + "\u00a7b=" + spread + "×" + (base + scale * levelScalingFactor)) + " \u00a7c@" + random + "\u00a7e = " + (base + scale * levelScalingFactor) * (1 + Math.min(Math.max(random * spread, -maxSpread), maxSpread)));
			return (base + scale * levelScalingFactor) * (1 + Math.min(Math.max(random * spread, -maxSpread), maxSpread));
		}

		// The mean, the center of the distribution
		double actualBase = base + (scale * levelScalingFactor);

		/*
		 * This is one pick from a gaussian distribution
		 * at mean 0, and standard deviation 1, multiplied
		 * by the spread chosen.
		 */
		double flatSpread = random * spread;

		// Does it exceed the max spread (positive or negative)? Not anymore!
		flatSpread = Math.min(Math.max(flatSpread, -maxSpread), maxSpread);

		// That's it
		//SPRD//if (spread > 0) MMOItems.log("\u00a7c༺\u00a77 Using \u00a7aAdditive\u00a77 spread formula, \u00a76μ=" + (base + scale * levelScalingFactor) + "\u00a77, \u00a73σ=" + (spread)  + " \u00a7c@" + random + "\u00a7e = " + (actualBase + gaussSpread));
		return actualBase + flatSpread;
	}

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		return new DoubleData(calculate(builder.getLevel()));
	}

	/**
	 * Save some formula in a config file. This method is used when editing stat
	 * data in the edition GUI (when a player inputs a numeric formula)
	 * 
	 * @param config The formula will be saved in that config file
	 * @param path   The config path used to save the formula
	 */
	public void fillConfigurationSection(ConfigurationSection config, String path, FormulaSaveOption option) {
		if (path == null)
			throw new NullPointerException("Path was empty");

		if (scale == 0 && spread == 0 && maxSpread == 0)
			config.set(path, base == 0 && option == FormulaSaveOption.DELETE_IF_ZERO ? null : base);

		else {
			config.set(path + ".base", base);
			config.set(path + ".scale", scale);
			config.set(path + ".spread", spread);
			config.set(path + ".max-spread", maxSpread);
		}
	}

	public void fillConfigurationSection(ConfigurationSection config, String path) {
		fillConfigurationSection(config, path, FormulaSaveOption.DELETE_IF_ZERO);
	}

	@Override
	public String toString() {

		if (scale == 0 && spread == 0)
			return DIGIT.format(base);

		if (scale == 0)
			return "[" + DIGIT.format(base * (1 - maxSpread)) + " -> " + DIGIT.format(base * (1 + maxSpread)) + "] (" + DIGIT.format(spread * 100)
					+ "% Spread) (" + DIGIT.format(base) + " Avg)";

		return "{Base=" + DIGIT.format(base) + (scale != 0 ? ",Scale=" + DIGIT.format(scale) : "") + (spread != 0 ? ",Spread=" + spread : "")
				+ (maxSpread != 0 ? ",Max=" + maxSpread : "") + "}";
	}

	public static void reload() { useRelativeSpread = !MMOItems.plugin.getConfig().getBoolean("additive-spread-formula", false); }

	@NotNull
	@SuppressWarnings("unchecked")
	@Override
	public <T extends StatData> T reroll(@NotNull ItemStat stat, @NotNull T original, int determinedItemLevel) {
		//UPGRD//MMOItems.log("\u00a7a +\u00a77 Valid for Double Data procedure\u00a78 {Original:\u00a77 " + ((DoubleData) original).getValue() + "\u00a78}");

		// Very well, chance checking is only available for NumericStatFormula class so
		double scaledBase = getBase() + (getScale() * determinedItemLevel);

		// Determine current
		double current = ((DoubleData) original).getValue();

		// What was the shift?
		double shift = current - scaledBase;

		// How many standard deviations away?
		double sD = Math.abs(shift / getSpread());
		if (useRelativeSpread) { sD = Math.abs(shift / (getSpread() * scaledBase)); }
		//UPGRD//MMOItems.log("\u00a7b *\u00a77 Base: \u00a73" + base);
		//UPGRD//MMOItems.log("\u00a7b *\u00a77 Curr: \u00a73" + current);
		//UPGRD//MMOItems.log("\u00a7b *\u00a77 Shft: \u00a73" + shift);
		//UPGRD//MMOItems.log("\u00a7b *\u00a77 SDev: \u00a73" + sD);

		// Greater than max spread? Or heck, 0.1% Chance or less wth
		if (sD > getMaxSpread() || sD > 3.5) {
			//UPGRD//MMOItems.log("\u00a7c -\u00a77 Ridiculous Range --- reroll");

			// Adapt within reason
			double reasonableShift = getSpread() * Math.min(2, getMaxSpread());
			if (shift < 0) { reasonableShift *= -1;}

			// That's the data we'll use
			return (T) new DoubleData(reasonableShift + scaledBase);

			// Data arguably fine tbh, just use previous
		} else {
			//UPGRD//MMOItems.log("\u00a7a +\u00a77 Acceptable Range --- kept");

			// Just clone I guess
			return (T) ((Mergeable) original).cloneData(); }
	}

	public enum FormulaSaveOption {

		/**
		 * When toggled on, if the formula is set to 0 then the configuration
		 * section will just be deleted. This option fixes a bug where ability
		 * modifiers with non null default values cannot take strictly null
		 * values because inputting 0 would just delete the config section.
		 */
		DELETE_IF_ZERO,

		/**
		 * No option used
		 */
		NONE;
	}
}
