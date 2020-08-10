package net.Indyuce.mmoitems.api.item.mmoitem;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.ItemReference;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class MMOItem implements ItemReference {
	private final Type type;
	private final String id;

	/*
	 * where data about all the item stats is stored. when the item is
	 * generated, this map is read and all the stats are applied. the order in
	 * which stats are added is very important because the material needs to be
	 * applied first
	 */
	private final Map<ItemStat, StatData> stats = new LinkedHashMap<>();

	/**
	 * Constructor used to generate an ItemStack based on some stat data
	 * 
	 * @param type
	 *            The type of the item you want to create
	 * @param id
	 *            The id of the item, make sure it is different from other
	 *            existing items not to interfere with MI features like the
	 *            dynamic item updater
	 */
	public MMOItem(Type type, String id) {
		this.type = type;
		this.id = id;
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setData(ItemStat stat, StatData data) {
		stats.put(stat, data);
	}

	public void replaceData(ItemStat stat, StatData data) {
		stats.replace(stat, data);
	}

	public void removeData(ItemStat stat) {
		stats.remove(stat);
	}

	public StatData getData(ItemStat stat) {
		return stats.get(stat);
	}

	public boolean hasData(ItemStat stat) {
		return stats.containsKey(stat);
	}

	/**
	 * @return Collection of all item stats which have some data on this mmoitem
	 */
	public Set<ItemStat> getStats() {
		return stats.keySet();
	}

	/**
	 * @return A class which lets you build this mmoitem into an ItemStack
	 */
	public ItemStackBuilder newBuilder() {
		return new ItemStackBuilder(this);
	}

	/***
	 * @return A cloned instance of this mmoitem. This does NOT clone the
	 *         StatData instances! If you edit these statDatas, the previous
	 *         mmoitem will be edited as well.
	 */
	public MMOItem clone() {
		MMOItem clone = new MMOItem(type, id);
		stats.forEach((stat, data) -> clone.stats.put(stat, data));
		return clone;
	}
}
