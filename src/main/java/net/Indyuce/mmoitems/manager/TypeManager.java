package net.Indyuce.mmoitems.manager;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.manager.ConfigManager.DefaultFile;

public class TypeManager {
	private final Map<String, Type> map = new LinkedHashMap<>();

	/**
	 * Reloads the type manager. It entirely empties the currently registered
	 * item types, registers default item types again and reads item-types.yml
	 */
	public void reload() {
		map.clear();
		registerAll(Type.ACCESSORY, Type.ARMOR, Type.BOW, Type.CATALYST, Type.CONSUMABLE, Type.CROSSBOW, Type.DAGGER, Type.GAUNTLET, Type.GEM_STONE,
				Type.SKIN, Type.HAMMER, Type.LUTE, Type.MISCELLANEOUS, Type.MUSKET, Type.OFF_CATALYST, Type.ORNAMENT, Type.SPEAR, Type.STAFF,
				Type.SWORD, Type.TOOL, Type.WHIP);

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
					register(new Type(this, config.getConfig().getConfigurationSection(id)));
				} catch (IllegalArgumentException exception) {
					MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register type '" + id + "': " + exception.getMessage());
				}

		for (Iterator<Type> iterator = map.values().iterator(); iterator.hasNext();) {
			Type type = iterator.next();

			try {
				type.load(config.getConfig().getConfigurationSection(type.getId()));
			} catch (IllegalArgumentException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not register type '" + type.getId() + "': " + exception.getMessage());
				iterator.remove();
				continue;
			}

			/*
			 * caches all the stats which the type can have to reduce future
			 * both item generation (and GUI) calculations. probably the thing
			 * which takes the most time when loading item types.
			 */
			type.getAvailableStats().clear();
			MMOItems.plugin.getStats().getAll().stream().filter(stat -> stat.isCompatible(type)).forEach(stat -> type.getAvailableStats().add(stat));
		}
	}

	public void register(Type type) {
		map.put(type.getId(), type);
	}

	private void registerAll(Type... types) {
		for (Type type : types)
			register(type);
	}

	// TODO minor refactor, use Optional<Type>
	public Type get(String id) {
		return map.get(id);
	}

	public Type getOrThrow(String id) {
		Validate.isTrue(map.containsKey(id), "Could not find item type with ID '" + id + "'");
		return get(id);
	}

	public boolean has(String id) {
		return map.containsKey(id);
	}

	public Collection<Type> getAll() {
		return map.values();
	}
}
