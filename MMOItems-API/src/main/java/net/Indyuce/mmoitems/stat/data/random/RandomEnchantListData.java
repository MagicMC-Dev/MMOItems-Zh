package net.Indyuce.mmoitems.stat.data.random;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.Enchants;
import net.Indyuce.mmoitems.stat.data.EnchantListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RandomEnchantListData implements RandomStatData<EnchantListData> {
	private final Map<Enchantment, NumericStatFormula> enchants = new HashMap<>();

	public RandomEnchantListData(ConfigurationSection config) {
		Validate.notNull(config, "配置不能为空");

		for (String key : config.getKeys(false)) {
			Enchantment enchant = Enchants.getEnchant(key);
			Validate.notNull(enchant, "找不到附魔: '" + key + "'");
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
	public EnchantListData randomize(MMOItemBuilder builder) {
		EnchantListData list = new EnchantListData();
		enchants.forEach((enchant, formula) -> list.addEnchant(enchant, (int) Math.max(formula.calculate(builder.getLevel()), enchant.getStartLevel())));
		return list;
	}
}
