package net.Indyuce.mmoitems.manager;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.manager.ConfigManager.DefaultFile;

public class TypeManager {
	private final Map<String, Type> map = new LinkedHashMap<>();

	public TypeManager() {
		reload();
	}

	public void reload() {
		map.clear();
		addAll(Type.ACCESSORY, Type.ARMOR, Type.BOW, Type.CATALYST, Type.CONSUMABLE, Type.CROSSBOW, Type.DAGGER,
			Type.GAUNTLET,Type.GEM_STONE, Type.HAMMER, Type.LUTE, Type.MISCELLANEOUS, Type.MUSKET, Type.OFF_CATALYST,
			Type.ORNAMENT, Type.SPEAR, Type.STAFF, Type.SWORD, Type.TOOL, Type.WHIP);

		/*
		 * register all other types. important: check if the map already
		 * contains the id, this way the DEFAULT types are not registered twice,
		 * and only custom types are registered with a parent.
		 */
		DefaultFile.ITEM_TYPES.checkFile();

		ConfigFile config = new ConfigFile("item-types");
		for (String id : config.getConfig().getKeys(false))
			if (!map.containsKey(id))
				try {
					add(new Type(this, config.getConfig().getConfigurationSection(id)));
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register the type " + id + ": " + exception.getMessage());
				}

		/*
		 * reload names & display items from types and generate corresponding
		 * config files.
		 */
		for (Iterator<Type> iterator = map.values().iterator(); iterator.hasNext();) {
			Type type = iterator.next();

			try {
				type.load(config.getConfig().getConfigurationSection(type.getId()));
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register the type " + type.getId() + ": " + exception.getMessage());
				iterator.remove();
				continue;
			}

			String path = type.getId().toLowerCase().replace("_", "-");
			if (!config.getConfig().contains(path))
				config.getConfig().set(path, type.getName());

			/*
			 * caches all the stats which the type can have to reduce future
			 * both item generation (and GUI) calculations. probably the thing
			 * which takes the most time when loading item types.
			 */
			type.cacheAvailableStats(MMOItems.plugin.getStats().getAll().stream().filter(stat -> type.canHave(stat)).collect(Collectors.toList()));
		}
	}

	public void add(Type type) {
		map.put(type.getId(), type);
	}

	public void addAll(Type... types) {
		for (Type type : types)
			add(type);
	}

	public Type get(String id) {
		return map.get(id);
	}

	public boolean has(String id) {
		return map.containsKey(id);
	}

	public Collection<Type> getAll() {
		return map.values();
	}
}
