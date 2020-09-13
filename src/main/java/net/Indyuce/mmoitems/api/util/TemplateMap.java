package net.Indyuce.mmoitems.api.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.api.Type;

/**
 * Allows the use of two nested maps to efficiently store data about mmoitem
 * templates. The first nested map is for the item type, the second is for the
 * item ID.
 * 
 * @author cympe
 *
 * @param <C>
 *            The class of the value you want to assign to every mmoitem
 *            template
 */
public class TemplateMap<C> {
	private final Map<String, Submap> typeMap = new HashMap<>();

	/**
	 * @param type
	 *            The item type
	 * @param id
	 *            The template identifier
	 * @return If a template has some value stored in that map
	 */
	public boolean hasValue(Type type, String id) {
		return typeMap.containsKey(type.getId()) && typeMap.get(type.getId()).idMap.containsKey(id);
	}

	/**
	 * @param type
	 *            The item type
	 * @param id
	 *            The template identifier
	 * @return Returns the value stored in the template map
	 */
	public C getValue(Type type, String id) {
		return typeMap.get(type.getId()).idMap.get(id);
	}

	/**
	 * Unregisters a value from the map
	 * 
	 * @param type
	 *            The item type
	 * @param id
	 *            The template identifier
	 */
	public void removeValue(Type type, String id) {
		if (typeMap.containsKey(type.getId()))
			typeMap.get(type.getId()).idMap.remove(id);
	}

	/**
	 * Registers a value for a specific mmoitem template
	 * 
	 * @param type
	 *            The item type
	 * @param id
	 *            The template identifier
	 * @param value
	 *            The value to registered
	 */
	public void setValue(Type type, String id, C value) {
		Validate.notNull(value, "Value cannot be null");

		if (!typeMap.containsKey(type.getId()))
			typeMap.put(type.getId(), new Submap());
		typeMap.get(type.getId()).idMap.put(id, value);
	}

	/**
	 * Applies a specific consumer for every template. This is used to postload
	 * all templates when MMOItems enables
	 * 
	 * @param action
	 *            Action performed for every registered template
	 */
	public void forEach(Consumer<C> action) {
		typeMap.values().forEach(submap -> submap.idMap.values().forEach(action));
	}

	/**
	 * @return Collects all the values registered in this template map.
	 */
	public Collection<C> collectValues() {
		Set<C> collected = new HashSet<>();
		typeMap.values().forEach(submap -> collected.addAll(submap.idMap.values()));
		return collected;
	}

	/**
	 * @param type
	 *            The item type
	 * @return Collects all the values registered in this template map with a
	 *         specific item type
	 */
	public Collection<C> collectValues(Type type) {
		return typeMap.containsKey(type.getId()) ? typeMap.get(type.getId()).idMap.values() : new HashSet<>();
	}

	/**
	 * Clears the map
	 */
	public void clear() {
		typeMap.clear();
	}

	/**
	 * For memory leak purposes we cannot directly use a nested map into another
	 * map (must resort to using an object). No method is required however
	 * because this class is completely private.
	 * 
	 * @author cympe
	 *
	 */
	private class Submap {
		private final Map<String, C> idMap = new LinkedHashMap<>();
	}
}
