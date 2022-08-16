package net.Indyuce.mmoitems.api.util;

import net.Indyuce.mmoitems.api.Type;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

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
	private final Map<Type, Submap> typeMap = new HashMap<>();

	/**
	 * @param type
	 *            The item type
	 * @param id
	 *            The template identifier
	 * @return If a template has some value stored in that map
	 */
	public boolean hasValue(@Nullable Type type, @Nullable String id) {
		if(type == null || id == null) { return false; }
		return typeMap.containsKey(type) && typeMap.get(type).idMap.containsKey(id);
	}

	/**
	 * @param type
	 *            The item type
	 * @param id
	 *            The template identifier
	 * @return Returns the value stored in the template map
	 */
	@Nullable public C getValue(@Nullable Type type, @Nullable String id) {
		if(type == null || id == null) { return null; }
		Submap m = typeMap.get(type);
		if (m == null) { return null; }
		return m.idMap.get(id);
	}

	/**
	 * Unregisters a value from the map
	 * 
	 * @param type
	 *            The item type
	 * @param id
	 *            The template identifier
	 */
	public void removeValue(@Nullable Type type, @Nullable String id) {
		if(type == null || id == null) { return; }
		if (typeMap.containsKey(type))
			typeMap.get(type).idMap.remove(id);
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
	public void setValue(@NotNull Type type, @NotNull String id, @NotNull C value) {
		Validate.notNull(value, "Value cannot be null");

		if (!typeMap.containsKey(type))
			typeMap.put(type, new Submap());
		typeMap.get(type).idMap.put(id, value);
	}

	/**
	 * Applies a specific consumer for every template. This is used to postload
	 * all templates when MMOItems enables
	 * 
	 * @param action
	 *            Action performed for every registered template
	 */
	public void forEach(@NotNull Consumer<C> action) { typeMap.values().forEach(submap -> submap.idMap.values().forEach(action)); }

	/**
	 * @return Collects all the values registered in this template map.
	 */
	@NotNull public Collection<C> collectValues() {
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
	@NotNull public Collection<C> collectValues(@NotNull Type type) {
		return typeMap.containsKey(type) ? typeMap.get(type).idMap.values() : new HashSet<>();
	}

	/**
	 * Clears the map
	 */
	public void clear() { typeMap.clear(); }

	/**
	 * For memory leak purposes we cannot directly use a nested map into another
	 * map (must resort to using an object). No methods are required however
	 * because this class is completely private.
	 * 
	 * @author cympe
	 *
	 */
	private class Submap {
		private final Map<String, C> idMap = new LinkedHashMap<>();
	}
}
