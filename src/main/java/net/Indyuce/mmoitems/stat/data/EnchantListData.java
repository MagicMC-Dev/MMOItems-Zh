package net.Indyuce.mmoitems.stat.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.enchantments.Enchantment;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class EnchantListData implements StatData, Mergeable {
	private final Map<Enchantment, Integer> enchants = new HashMap<>();

	public Set<Enchantment> getEnchants() {
		return enchants.keySet();
	}

	public int getLevel(Enchantment enchant) {
		return enchants.get(enchant);
	}

	public void addEnchant(Enchantment enchant, int level) {
		enchants.put(enchant, level);
	}

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof EnchantListData, "Cannot merge two different stat data types");
		Map<Enchantment, Integer> extra = ((EnchantListData) data).enchants;
		for (Enchantment enchant : extra.keySet())
			enchants.put(enchant, enchants.containsKey(enchant) ? Math.max(extra.get(enchant), enchants.get(enchant)) : extra.get(enchant));
	}
}