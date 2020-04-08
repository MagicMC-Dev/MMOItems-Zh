package net.Indyuce.mmoitems.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class TierManager {
	private final Map<String, ItemTier> tiers = new HashMap<>();

	public TierManager() {
		reload();
	}

	public void reload() {
		tiers.clear();

		ConfigFile config = new ConfigFile("item-tiers");
		for (String key : config.getConfig().getKeys(false))
			try {
				register(new ItemTier(config.getConfig().getConfigurationSection(key)));
			} catch (Exception exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load item tier " + key);
			}
	}

	public void register(ItemTier tier) {
		tiers.put(tier.getId(), tier);
	}

	public boolean has(String id) {
		return tiers.containsKey(id);
	}

	public ItemTier get(String id) {
		return tiers.get(id);
	}

	public Collection<ItemTier> getAll() {
		return tiers.values();
	}

	public ItemTier findTier(MMOItem item) {
		try {
			return item.hasData(ItemStat.TIER) ? get(item.getData(ItemStat.TIER).toString()) : null;
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}
}
