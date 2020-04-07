package net.Indyuce.mmoitems.api.item;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.NBTItem;

public class MMOItem {

	// MMOItem data
	private String id;
	private Type type;

	/*
	 * where data about all the item stats is stored. when the item is
	 * generated, this map is read and all the stats are applied. the order in
	 * which stats are added is very important because the material needs to be
	 * applied first
	 */
	private final Map<ItemStat, StatData> stats = new LinkedHashMap<>();

	/*
	 * can be null if it has not been initialized in the constructor.
	 */
	private NBTItem item;
	private boolean load;

	public MMOItem(NBTItem item) {
		this(item, true);
	}

	public MMOItem(NBTItem item, boolean load) {
		this.item = item;
		this.load = load;

		if (load) {
			type = item.getType();
			id = item.getString("MMOITEMS_ITEM_ID");

			for (ItemStat stat : type.getAvailableStats())
				stat.whenLoaded(this, item);
		}
	}

	public MMOItem(Type type, String id) {
		this.type = type;
		this.id = id;
	}

	public boolean hasNBTItem() {
		return item != null;
	}

	public NBTItem getNBTItem() {
		return item;
	}

	public String getId() {
		return id;
	}

	public Type getType() {
		return type;
	}

	public void setItemInfo(Type type, String id) {
		this.type = type;
		this.id = id;
	}

	public MMOItemBuilder newBuilder() {
		return new MMOItemBuilder(this);
	}

	public void log(Level level, String message) {
		MMOItems.plugin.getLogger().log(level, type.getId() + "." + id + " - " + message);
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
		if (!load && !stats.containsKey(stat) && item != null)
			stat.whenLoaded(this, item);
		return stats.containsKey(stat);
	}

	public Set<ItemStat> getStats() {
		return stats.keySet();
	}
}
