package net.Indyuce.mmoitems.manager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.stat.type.AttributeStat;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemRestriction;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.ProperStat;

public class StatManager {
	private final Map<String, ItemStat> stats = new LinkedHashMap<>();

	/*
	 * numeric statistics handled by MMOLib
	 */
	private final Set<DoubleStat> numeric = new HashSet<>();

	private final Set<AttributeStat> attributeBased = new HashSet<>();
	private final Set<ItemRestriction> itemRestriction = new HashSet<>();

	/*
	 * load default stats using java reflection, get all public static final
	 * fields in the ItemStat and register them as stat instances
	 */
	public StatManager() {
		for (Field field : ItemStat.class.getFields())
			try {
				if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && field.get(null) instanceof ItemStat)
					register(field.getName(), (ItemStat) field.get(null));
			} catch (IllegalArgumentException | IllegalAccessException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Couldn't register stat called " + field.getName());
			}
	}

	public Collection<ItemStat> getAll() {
		return stats.values();
	}

	/*
	 * cache specific stats for better performance using these extra sets
	 */
	public Set<AttributeStat> getAttributeStats() {
		return attributeBased;
	}

	public Set<DoubleStat> getNumericStats() {
		return numeric;
	}

	public Set<ItemRestriction> getItemRestrictionStats() {
		return itemRestriction;
	}

	public boolean has(String id) {
		return stats.containsKey(id);
	}

	/*
	 * the extra checks in that method to register stats even after the plugin
	 * has successfully enabled otherwise the other sets would not be updated.
	 */
	public void register(String id, ItemStat stat) {
		if (!stat.isEnabled())
			return;

		stats.put(stat.getId(), stat);

		if (stat instanceof DoubleStat && !(stat instanceof ProperStat) && Type.GEM_STONE.canHave(stat))
			numeric.add((DoubleStat) stat);

		if (stat instanceof AttributeStat)
			attributeBased.add((AttributeStat) stat);

		if (stat instanceof ItemRestriction)
			itemRestriction.add((ItemRestriction) stat);

		/*
		 * cache stat for every type which may have this stat. really important
		 * otherwise the stat will NOT be used anywhere in the plugin.
		 */
		if (MMOItems.plugin.getTypes() != null)
			for (Type type : MMOItems.plugin.getTypes().getAll())
				if (type.canHave(stat))
					type.getAvailableStats().add(stat);
	}

	public ItemStat get(String str) {
		return stats.containsKey(str) ? stats.get(str) : null;
	}
}
