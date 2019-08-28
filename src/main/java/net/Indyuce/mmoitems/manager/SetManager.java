package net.Indyuce.mmoitems.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.ItemSet;

public class SetManager {
	private Map<String, ItemSet> itemSets = new HashMap<>();

	public SetManager() {
		reload();
	}

	public void reload() {
		ConfigFile config = new ConfigFile("item-sets");

		// reload item sets and cache them into a map
		itemSets.clear();
		for (String id : config.getConfig().getKeys(false))
			itemSets.put(id, new ItemSet(config.getConfig().getConfigurationSection(id)));
	}

	public Collection<ItemSet> getAll() {
		return itemSets.values();
	}

	public ItemSet get(String id) {
		return itemSets.containsKey(id) ? itemSets.get(id) : null;
	}
}
