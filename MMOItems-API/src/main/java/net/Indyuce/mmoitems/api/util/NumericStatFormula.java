package net.Indyuce.mmoitems.api.util;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.random.UpdatableRandomStatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * That Gaussian spread distribution thing that no one understands.
 *
 * @author indyuce
 */
public class NumericStatFormula implements RandomStatData<DoubleData>, UpdatableRandomStatData<DoubleData> {
    private final double base, scale, spread, maxSpread, min, max;
    private final boolean uniform, hasMin, hasMax;

    private static final Random RANDOM = new Random();
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.####");

    public static final NumericStatFormula ZERO = new NumericStatFormula(0, 0, 0, 0);

    private final static double DEFAULT_MAX_SPREAD = .3;

    /**
     * When reading a numeric stat formula either from a config file
     * (configuration section, or number) or when reading player input when a
     * player edits a stat (string message). Although the string format would
     * work in the config as well.
     * <p>
     * Throws an IAE either if the format is not good or if the object does not
     * have the right type.
     *
     * @param object Object to read data from.
     */
    public NumericStatFormula(Object object) {
        Validate.notNull(object, "Config must not be null");

        if (object instanceof String) {

            // Uniform range from string
            if (object.toString().contains("->")) {
                String[] split = object.toString().replace(" ", "").split(Pattern.quote("->"));
                base = 0;
                scale = 0;
                spread = 0;
                maxSpread = 0;
                uniform = true;
                hasMin = true;
                hasMax = true;
                min = Double.parseDouble(split[0]);
                max = Double.parseDouble(split[1]);
            }

            // Gaussian distribution from string
            else {
                String[] split = object.toString().split(" ");
                base = Double.parseDouble(split[0]);
                scale = split.length > 1 ? Double.parseDouble(split[1]) : 0;
                spread = split.length > 2 ? Double.parseDouble(split[2]) : 0;
                maxSpread = split.length > 3 ? Double.parseDouble(split[3]) : 0;
                hasMin = split.length > 4;
                hasMax = split.length > 5;
                min = hasMin ? Double.parseDouble(split[4]) : 0;
                max = hasMax ? Double.parseDouble(split[5]) : 0;
                uniform = false;
            }
        }

        // Constant
        else if (object instanceof Number) {
            base = Double.parseDouble(object.toString());
            scale = 0;
            spread = 0;
            maxSpread = 0;
            uniform = false;
            hasMin = false;
            hasMax = false;
            min = 0;
            max = 0;
        }

        // Load from config section
        else if (object instanceof ConfigurationSection) {
            ConfigurationSection config = (ConfigurationSection) object;
            base = config.getDouble("base");
            scale = config.getDouble("scale");
            spread = config.getDouble("spread");
            maxSpread = config.getDouble("max-spread", DEFAULT_MAX_SPREAD);
            hasMin = config.contains("min");
            hasMax = config.contains("max");
            uniform = !config.contains("spread") && !config.contains("scale") && !config.contains("base") && hasMin && hasMax;
            min = hasMin ? config.getDouble("min") : 0;
            max = hasMax ? config.getDouble("max") : 0;
        }

        // Error
        else {
            throw new IllegalArgumentException("Must specify a config section, a string or a number");
        }

        // Validates
        Validate.isTrue(spread >= 0, "Spread must be positive");
        Validate.isTrue(maxSpread >= 0, "Max spread must be positive");
    }

    /**
     * A gaussian distribution with spread-based boundaries
     */
    public NumericStatFormula(double base, double scale, double spread, double maxSpread) {
        this(base, scale, spread, maxSpread, false, false, 0, false, 0);
    }

    /**
     * A gaussian distribution with constant boundaries
     */
    public NumericStatFormula(double base, double scale, double spread, double min, double max) {
        this(base, scale, spread, 100, false, true, min, true, max);
    }

    /**
     * Formula for numerical statistics. These formulas allow stats to scale
     * accordingly to the item level but also have a more or less important
     * gaussian/uniform based random factor.
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
     * @param hasMin    Should the value have a lower threshold
     * @param min       Lower bound for numerical value
     * @param hasMax    Should the value have an  upper threshold
     * @param min       Upper bound for numerical value
     */
    public NumericStatFormula(double base, double scale, double spread, double maxSpread,
                              boolean uniform, boolean hasMin, double min, boolean hasMax, double max) {
        this.base = base;
        this.scale = scale;
        this.spread = spread;
        this.maxSpread = maxSpread;
        this.uniform = uniform;
        this.hasMin = hasMin;
        this.hasMax = hasMax;
        this.min = min;
        this.max = max;
    }

    /**
     * @return The 'Base' number of the item. This chooses the peak of the Gaussian Distribution.
     * @see #getScale()
     */
    public double getBase() {
        return base;
    }

    /**
     * @return When the item has a certain level or tier, this is how much each level shifts
     *         the peak, so that it is centered at {@code base + scale*level}
     * @see #getBase()
     */
    public double getScale() {
        return scale;
    }

    /**
     * @return Standard Deviation of the Gaussian Distribution
     */
    public double getSpread() {
        return spread;
    }

    /**
     * @return For gaussian distributions, there always is that INSANELY SMALL
     *         chance of getting an INSANELY LARGE number.
     *         <p>
     *         For example: At base atk dmg 10, and standard deviation 1:
     *         <p>68% of rolls will fall between 9 and 11;
     *         </p>95% of rolls will fall between 8 and 12;
     *         <p>99.7% of rolls will fall between 7 and 13;
     *         </p>10E-42 of a roll that will give you an epic 300 dmg sword
     *         <p></p>
     *         Whatever, this constrains to a minimum and maximum of output.
     */
    public double getMaxSpread() {
        return maxSpread;
    }

    public boolean isUniform() {
        return uniform;
    }

    public boolean hasMin() {
        return hasMin;
    }

    public boolean hasMax() {
        return hasMax;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public static boolean RELATIVE_SPREAD = false;

    /**
     * Applies the formula for a given input x.
     *
     * @param scaleFactor When choosing the mean of the distribution,
     *                    the formula is <code>base + (scale*level)</code>.
     *                    This is the <code>level</code>
     * @return <b>Legacy formula: ???</b><br>
     *         Let A = {base} + {scale} * lvl, then the returned value is a
     *         random value taken in respect to a gaussian distribution
     *         centered on A, with average spread of {spread}%, and with a
     *         maximum offset of {maxSpread}% (relative to average value)
     *         <p></p>
     *         <b>Formula: Spread = Standard Deviation</b>
     *         The mean, the peak is located at <code>{base} + {scale}*lvl</code>. <br>
     *         The 'spread' is the standard deviation of the distribution. <br>
     *         'Max Spread' constrains the result of this operation at <code>{mean}±{max spread}</code>
     */
    public double calculate(double scaleFactor) {

        // Calculate yes
        return calculate(scaleFactor, FormulaInputType.RANDOM);
    }

    public double calculate(double scaleFactor, @NotNull FormulaInputType type) {
        return calculate(scaleFactor,
                type == FormulaInputType.RANDOM ? (uniform ? RANDOM.nextDouble() : RANDOM.nextGaussian()) :
                        type == FormulaInputType.UPPER_BOUND ? (uniform ? 1 : 2.5) :
                                type == FormulaInputType.LOWER_BOUND ? (uniform ? 0 : -2.5) : Double.NaN);
    }

    /**
     * @param scaleFactor Level to scale the scale with
     * @param random      Result of <code>RANDOM.nextGaussian()</code> or whatever other
     *                    value that you actually want to pass. It can be any valuation of
     *                    a random variable with mean 0 and variance 1.
     * @return The calculated final numerical value
     */
    public double calculate(double scaleFactor, double random) {

        if (uniform) {
            // Spread, max-spread and base are ignored
            return scaleFactor * scale + min + (max - min) * random;
        }

        // Gausian
        else {

            // The mean, the center of the distribution
            final double actualBase = base + (scale * scaleFactor);

            /*
             * This is one pick from a gaussian distribution at mean 0, and
             * standard deviation 1, multiplied by the spread chosen.
             * Does it exceed the max spread (positive or negative)? Not anymore!
             */
            final double spreadCoef = Math.min(Math.max(random * spread, -maxSpread), maxSpread);

            double value = RELATIVE_SPREAD ? actualBase * (1 + spreadCoef) : actualBase + spreadCoef;
            if (hasMin) value = Math.max(min, value);
            if (hasMax) value = Math.min(max, value);
            return value;
        }
    }

    public static enum FormulaInputType {

        /**
         * Upper bound of gaussian/uniform formula
         */
        UPPER_BOUND,

        /**
         * Lower bound of gaussian/uniform formula
         */
        LOWER_BOUND,

        /**
         * Random value, for stat value generation
         */
        RANDOM;
    }

    @Override
    public DoubleData randomize(MMOItemBuilder builder) {
        return new DoubleData(calculate(builder.getLevel()));
    }

    /**
     * Save some formula in a config file. This method is used when editing stat
     * data in the edition GUI (when a player inputs a numeric formula).
     *
     * @param config The formula will be saved in that config file
     * @param path   The config path used to save the formula
     * @param option If zero formulas should be ignored
     */
    public void fillConfigurationSection(@NotNull ConfigurationSection config, @NotNull String path, @NotNull FormulaSaveOption option) {
        if (path == null)
            throw new NullPointerException("Path is empty");

        if (scale == 0 && spread == 0 && maxSpread == 0 && !uniform) {
            config.set(path, base == 0 && option == FormulaSaveOption.DELETE_IF_ZERO ? null : base);
            return;
        }

        config.createSection(path);
        config = config.getConfigurationSection(path);

        if (!uniform) {
            config.set("base", base);
            config.set("scale", scale);
            config.set("spread", spread);
            config.set("max-spread", maxSpread);
        }

        if (hasMin) config.set("min", min);
        if (hasMax) config.set("max", max);
    }

    public void fillConfigurationSection(ConfigurationSection config, String path) {
        fillConfigurationSection(config, path, FormulaSaveOption.DELETE_IF_ZERO);
    }

    @Override
    public String toString() {

        if (scale == 0 && spread == 0)
            return DECIMAL_FORMAT.format(base);

        if (scale == 0)
            return "[" + DECIMAL_FORMAT.format(base * (1 - maxSpread)) + " -> " + DECIMAL_FORMAT.format(base * (1 + maxSpread)) + "] (" + DECIMAL_FORMAT.format(spread * 100)
                    + "% Spread) (" + DECIMAL_FORMAT.format(base) + " Avg)";

        return "{Base=" + DECIMAL_FORMAT.format(base) + (scale != 0 ? ",Scale=" + DECIMAL_FORMAT.format(scale) : "") + (spread != 0 ? ",Spread=" + spread : "")
                + (maxSpread != 0 ? ",Max=" + maxSpread : "") + "}";
    }

    @NotNull
    @SuppressWarnings("unchecked")
    @Override
    public DoubleData reroll(@NotNull ItemStat stat, @NotNull DoubleData original, int determinedItemLevel) {

        // Very well, chance checking is only available for NumericStatFormula class
        final double expectedValue = getBase() + (getScale() * determinedItemLevel);
        final double previousValue = original.getValue();
        final double shift = previousValue - expectedValue;
        final double shiftSD = RELATIVE_SPREAD ? Math.abs(shift / (getSpread() * expectedValue)) : Math.abs(shift / getSpread());
        final double maxSD = getMaxSpread() / getSpread();

        // Greater than max spread? Or heck, 0.1% Chance or less wth
        if (shiftSD > maxSD || shiftSD > 3.5) {

            // Just fully reroll value
            return new DoubleData(calculate(determinedItemLevel));

            // Data arguably fine tbh, just use previous
        } else {
            //UPGRD//MMOItems.log("\u00a7a +\u00a77 Acceptable Range --- kept");

            // Just clone I guess
            return original.cloneData();
        }
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
