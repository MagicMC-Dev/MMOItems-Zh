package net.Indyuce.mmoitems.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class ItemManager extends BukkitRunnable {
	private final Map<Type, CachedItems> cache = new HashMap<>();

	public ItemManager() {
		runTaskTimerAsynchronously(MMOItems.plugin, 60 * 20, 2 * 60 * 20);
	}

	public MMOItem getMMOItem(Type type, String id) {
		id = id.toUpperCase().replace("-", "_").replace(" ", "_");

		LoadedItem cached = getCachedMMOItem(type, id);
		if (cached != null) {
			cached.refresh();
			return cached.getItem();
		}

		FileConfiguration typeConfig = type.getConfigFile().getConfig();
		if (!typeConfig.contains(id))
			return null;

		MMOItem mmoitem = new MMOItem(type, id);
		ConfigurationSection section = typeConfig.getConfigurationSection(id);

		for (ItemStat stat : type.getAvailableStats())
			if (section.contains(stat.getPath()))
				try {
					mmoitem.setData(stat, stat.whenInitialized(section.get(stat.getPath())));
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.WARNING,
							"Error while loading " + type.getId() + "." + id + " (" + stat.getName() + "): " + exception.getMessage());
				}

		cache(mmoitem);
		return mmoitem;
	}

	public ItemStack getItem(Type type, String id) {
		MMOItem item = getMMOItem(type, id);
		return item == null ? null : item.newBuilder().build();
	}

	public LoadedItem getCachedMMOItem(Type type, String id) {
		CachedItems cached;
		return this.cache.containsKey(type) ? (cached = cache.get(type)).isCached(id) ? cached.getCached(id) : null : null;
	}

	/*
	 * warning, this method checks the entire config file and should only be
	 * used when the plugin is loading.
	 */
	public boolean hasMMOItem(Type type, String id) {
		id = id.toUpperCase().replace("-", "_").replace(" ", "_");

		// check cache
		if (cache.containsKey(type) && cache.get(type).isCached(id))
			return true;
		
		// check type config file
		return type.getConfigFile().getConfig().contains(id);
	}

	public void uncache(Type type, String id) {
		if (cache.containsKey(type))
			cache.get(type).emptyCache(id);
	}

	private void cache(MMOItem item) {
		if (!cache.containsKey(item.getType()))
			cache.put(item.getType(), new CachedItems());
		cache.get(item.getType()).cache(item.getId(), item);
	}

	/*
	 * every two minutes, loops through any loaded item and uncaches any if they
	 * have not been generated for more than 5 minutes.
	 */
	@Override
	public void run() {
		cache.values().forEach(cached -> cached.removeIf(loaded -> loaded.isTimedOut()));
	}

	public class CachedItems {
		private final Map<String, LoadedItem> cache = new HashMap<>();

		public LoadedItem getCached(String id) {
			return cache.get(id);
		}

		public boolean isCached(String id) {
			return cache.containsKey(id);
		}

		public void emptyCache(String id) {
			cache.remove(id);
		}

		public void cache(String id, MMOItem item) {
			cache.put(id, new LoadedItem(item));
		}

		public void removeIf(Predicate<LoadedItem> filter) {
			cache.values().removeIf(filter);
		}
	}

	public class LoadedItem {
		private final MMOItem item;

		private long loaded = System.currentTimeMillis();

		public LoadedItem(MMOItem item) {
			this.item = item;
		}

		public void refresh() {
			loaded = System.currentTimeMillis();
		}

		public MMOItem getItem() {
			return item;
		}

		public boolean isTimedOut() {
			return loaded + 5 * 60 * 1000 < System.currentTimeMillis();
		}
	}

	public void reload() {
		cache.clear();
	}
}
