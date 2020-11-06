package net.Indyuce.mmoitems.manager;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class TierManager implements Reloadable {
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
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load item tier '" + key + "': " + exception.getMessage());
			}
	}

	public void register(ItemTier tier) {
		tiers.put(tier.getId(), tier);
	}

	public boolean has(String id) {
		return tiers.containsKey(id);
	}

	public ItemTier getOrThrow(String id) {
		Validate.isTrue(tiers.containsKey(id), "Could not find tier with ID '" + id + "'");
		return tiers.get(id);
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
