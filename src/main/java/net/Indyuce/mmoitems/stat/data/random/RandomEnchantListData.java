package net.Indyuce.mmoitems.stat.data.random;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import net.Indyuce.mmoitems.api.itemgen.GeneratedItemBuilder;
import net.Indyuce.mmoitems.api.itemgen.NumericStatFormula;
import net.Indyuce.mmoitems.api.itemgen.RandomStatData;
import net.Indyuce.mmoitems.stat.data.EnchantListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class RandomEnchantListData implements RandomStatData {
	private final Map<Enchantment, NumericStatFormula> enchants = new HashMap<>();

	public RandomEnchantListData(ConfigurationSection config) {
		Validate.notNull(config, "Config cannot be null");

		for (String key : config.getKeys(false)) {
			Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(key.toLowerCase().replace("-", "_")));
			Validate.notNull(enchant, "Could not find enchant with key '" + key + "'");
			addEnchant(enchant, new NumericStatFormula(config.get(key)));
		}
	}

	public Set<Enchantment> getEnchants() {
		return enchants.keySet();
	}

	public NumericStatFormula getLevel(Enchantment enchant) {
		return enchants.get(enchant);
	}

	public void addEnchant(Enchantment enchant, NumericStatFormula formula) {
		enchants.put(enchant, formula);
	}

	@Override
	public StatData randomize(GeneratedItemBuilder builder) {
		EnchantListData list = new EnchantListData();
		enchants.forEach((enchant, formula) -> list.addEnchant(enchant, (int) formula.calculate(builder.getLevel())));
		return list;
	}
}
