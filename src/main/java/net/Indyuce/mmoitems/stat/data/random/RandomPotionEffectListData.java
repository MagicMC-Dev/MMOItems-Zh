package net.Indyuce.mmoitems.stat.data.random;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.PotionEffectListData;
import org.bukkit.potion.PotionEffectType;

public class RandomPotionEffectListData implements RandomStatData<PotionEffectListData> {
	private final List<RandomPotionEffectData> effects = new ArrayList<>();

	public RandomPotionEffectListData(ConfigurationSection config) {
		Validate.notNull(config, "Config cannot be null");

		// For every config section
		for (String key : config.getKeys(false)) {
			ConfigurationSection asSection = config.getConfigurationSection(key);

			// Valid?
			if (asSection != null) {

				this.effects.add(new RandomPotionEffectData(asSection));

			// Attempt legacy way: EFFECT: 22,8
			} else {

				// Must have at least two members
				String spl = config.getString(key);
				if (spl != null) {
					String[] split = spl.split(",");
					if (split.length >= 1) {

						// Firstone a double, scond an integer
						Double duration = SilentNumbers.DoubleParse(split[0]);
						Integer amplifier = SilentNumbers.IntegerParse(split[1]);
						PotionEffectType effect = PotionEffectType.getByName(key.toUpperCase().replace("-", "_").replace(" ", "_"));

						// Valid?
						if (duration != null && amplifier != null && effect != null) { 

							// Parsed OG
							effects.add(new RandomPotionEffectData(effect, new NumericStatFormula(duration), new NumericStatFormula(amplifier)));

						// L
						} else {
							throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Incorrect format, expected $e{Effect}: {Duration},{Amplifier}$b instead of $i{0} {1}$b.", key, spl));
						}

					// L
					} else {
						throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Incorrect format, expected $e{Effect}: {Duration},{Amplifier}$b instead of $i{0} {1}$b.", key, spl));
					}

				} else {

					throw new IllegalArgumentException("Config cannot be null");
				}
			}
		}
	}

	public RandomPotionEffectListData(RandomPotionEffectData... effects) {
		add(effects);
	}

	public void add(RandomPotionEffectData... effects) {
		this.effects.addAll(Arrays.asList(effects));
	}

	public List<RandomPotionEffectData> getEffects() {
		return effects;
	}

	@Override
	public PotionEffectListData randomize(MMOItemBuilder builder) {
		PotionEffectListData list = new PotionEffectListData();
		effects.forEach(random -> list.add(random.randomize(builder)));
		return list;
	}
}
