package net.Indyuce.mmoitems.stat.data.random;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmoitems.api.Element;
import net.Indyuce.mmoitems.api.item.template.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.ElementListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class RandomElementListData implements StatData, RandomStatData {
	private final Map<Element, NumericStatFormula> damage = new HashMap<>(), defense = new HashMap<>();

	public RandomElementListData(ConfigurationSection config) {
		Validate.notNull(config, "Config cannot be null");

		for (String key : config.getKeys(false)) {
			Element element = Element.valueOf(key.toUpperCase());
			if (config.contains(key + ".damage"))
				damage.put(element, new NumericStatFormula(config.get(key + ".damage")));
			if (config.contains(key + ".defense"))
				defense.put(element, new NumericStatFormula(config.get(key + ".defense")));
		}
	}

	public boolean hasDamage(Element element) {
		return damage.containsKey(element);
	}

	public boolean hasDefense(Element element) {
		return defense.containsKey(element);
	}

	public NumericStatFormula getDefense(Element element) {
		return defense.getOrDefault(element, NumericStatFormula.ZERO);
	}

	public NumericStatFormula getDamage(Element element) {
		return damage.getOrDefault(element, NumericStatFormula.ZERO);
	}

	public Set<Element> getDefenseElements() {
		return defense.keySet();
	}

	public Set<Element> getDamageElements() {
		return damage.keySet();
	}

	public void setDamage(Element element, NumericStatFormula formula) {
		damage.put(element, formula);
	}

	public void setDefense(Element element, NumericStatFormula formula) {
		defense.put(element, formula);
	}

	@Override
	public StatData randomize(MMOItemBuilder builder) {
		ElementListData elements = new ElementListData();
		damage.forEach((element, formula) -> elements.setDamage(element, formula.calculate(builder.getLevel())));
		defense.forEach((element, formula) -> elements.setDefense(element, formula.calculate(builder.getLevel())));
		return elements;
	}
}