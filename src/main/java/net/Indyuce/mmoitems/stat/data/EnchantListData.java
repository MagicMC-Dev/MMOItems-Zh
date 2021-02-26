package net.Indyuce.mmoitems.stat.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.Indyuce.mmoitems.MMOItems;
import org.apache.commons.lang.Validate;
import org.bukkit.enchantments.Enchantment;

import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnchantListData implements StatData, Mergeable {
	private final Map<Enchantment, Integer> enchants = new HashMap<>();

	public Set<Enchantment> getEnchants() {
		return enchants.keySet();
	}

	public int getLevel(@NotNull Enchantment enchant) {
		if (!enchants.containsKey(enchant)) { return 0; }
		return enchants.get(enchant);
	}

	public void addEnchant(Enchantment enchant, int level) {
		enchants.put(enchant, level);
	}
	public void clear() { enchants.clear(); }

	@Override
	public void merge(StatData data) {
		Validate.isTrue(data instanceof EnchantListData, "Cannot merge two different stat data types");
		boolean additiveMerge = MMOItems.plugin.getConfig().getBoolean("stat-merging.additive-enchantments", false);

		for (Enchantment enchant : ((EnchantListData) data).getEnchants()) {
			if (additiveMerge) {

				// Additive
				enchants.put(enchant, ((EnchantListData) data).getLevel(enchant) + enchants.get(enchant));
			} else {

				// Max Enchantment
				addEnchant(enchant,

						// Does this one already have the enchant?
						enchants.containsKey(enchant) ?

								// Use the better of the two
								Math.max(((EnchantListData) data).getLevel(enchant), enchants.get(enchant)) :

								// No enchant yet, just copy over
								((EnchantListData) data).getLevel(enchant));
			}
		}
	}

	@Override
	public @NotNull StatData cloneData() {

		// Start Fresh
		EnchantListData ret = new EnchantListData();

		// Enchant
		for (Enchantment enchant : enchants.keySet()) { ret.addEnchant(enchant, enchants.getOrDefault(enchant, 0)); }

		// Thats it
		return ret;
	}

}