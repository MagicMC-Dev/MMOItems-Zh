package net.Indyuce.mmoitems.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class ItemManager extends BukkitRunnable {
	private final Map<Type, Map<String, LoadedItem>> map = new HashMap<>();
	private final boolean cache;

	public ItemManager(boolean cache) {
		this.cache = cache;

		if (cache)
			runTaskTimerAsynchronously(MMOItems.plugin, 60 * 20, 2 * 60 * 20);
	}

	public void uncache(Type type, String id) {
		if (map.containsKey(type))
			map.get(type).remove(id);
	}

	public void cache(MMOItem item) {
		if (!map.containsKey(item.getType()))
			map.put(item.getType(), new HashMap<>());
		map.get(item.getType()).put(item.getId(), new LoadedItem(item));
	}

	public LoadedItem getCachedMMOItem(Type type, String id) {
		Map<String, LoadedItem> map;
		return this.map.containsKey(type) ? (map = this.map.get(type)).containsKey(id) ? map.get(id) : null : null;
	}

	public MMOItem getMMOItem(Type type, String id) {
		id = id.toUpperCase().replace("-", "_").replace(" ", "_");

		if (cache) {
			LoadedItem cached = getCachedMMOItem(type, id);
			if (cached != null) {
				cached.refresh();
				return cached.getItem();
			}
		}

		FileConfiguration items = type.getConfigFile().getConfig();
		if (!items.contains(id))
			return null;

		MMOItem mmoitem = new MMOItem(type, id);
		ConfigurationSection section = items.getConfigurationSection(id);

		for (ItemStat stat : type.getAvailableStats())
			if (section.contains(stat.getPath()) && !stat.whenLoaded(mmoitem, section))
				return null;

		if (cache)
			cache(mmoitem);
		return mmoitem;
	}

	public ItemStack getItem(Type type, String id) {
		MMOItem item = getMMOItem(type, id);
		// MMOItems.plugin.getLogger().log(Level.INFO, "Generated " +
		// type.getId() + "." + id);
		return item == null ? null : item.newBuilder().build();
	}

	/*
	 * every two minutes, loops through any loaded item and uncaches any if they
	 * have not been generated for more than 5 minutes.
	 */
	@Override
	public void run() {
		for (Type type : map.keySet()) {
			Map<String, LoadedItem> map = this.map.get(type);
			for (String id : new HashSet<>(map.keySet())) {
				LoadedItem item = map.get(id);
				if (item.isTimedOut())
					map.remove(id);
			}
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
		map.clear();
	}
}
