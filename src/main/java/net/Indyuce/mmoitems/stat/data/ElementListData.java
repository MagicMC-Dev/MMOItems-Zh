package net.Indyuce.mmoitems.stat.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.api.Element;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class ElementListData implements StatData, Mergeable {
	private final Map<Element, Double> damage = new HashMap<>(), defense = new HashMap<>();

	public double getDefense(Element element) {
		return defense.getOrDefault(element, 0d);
	}

	public double getDamage(Element element) {
		return damage.getOrDefault(element, 0d);
	}

	public Set<Element> getDefenseElements() {
		return defense.keySet();
	}

	public Set<Element> getDamageElements() {
		return damage.keySet();
	}

	public void setDamage(Element element, double value) {
		if (value == 0)
			damage.remove(element);
		else
			damage.put(element, value);
	}

	public void setDefense(Element element, double value) {
		if (value == 0)
			defense.remove(element);
		else
			defense.put(element, value);
	}

	public int total() {
		return damage.size() + defense.size();
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof ElementListData, "Cannot merge two different stat data types");
		ElementListData extra = (ElementListData) data;

		for (Element element : extra.damage.keySet())
			damage.put(element, extra.damage.get(element) + damage.getOrDefault(element, 0d));
		for (Element element : extra.defense.keySet())
			defense.put(element, extra.defense.get(element) + defense.getOrDefault(element, 0d));
	}
}