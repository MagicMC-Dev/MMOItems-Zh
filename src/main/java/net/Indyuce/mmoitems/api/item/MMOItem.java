package net.Indyuce.mmoitems.api.item;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class MMOItem {
	private final Type type;
	private final String id;

	/*
	 * where data about all the item stats is stored. when the item is
	 * generated, this map is read and all the stats are applied. the order in
	 * which stats are added is very important because the material needs to be
	 * applied first
	 */
	private final Map<ItemStat, StatData> stats = new LinkedHashMap<>();

	public MMOItem(Type type, String id) {
		this.type = type;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public Type getType() {
		return type;
	}

	public void setData(ItemStat stat, StatData data) {
		stats.put(stat, data);
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

	public Set<ItemStat> getStats() {
		return stats.keySet();
	}

	public MMOItemBuilder newBuilder() {
		return new MMOItemBuilder(this);
	}

	public MMOItem clone() {
		MMOItem clone = new MMOItem(type, id);
		stats.forEach((stat, data) -> clone.stats.put(stat, data));
		return clone;
	}
}
