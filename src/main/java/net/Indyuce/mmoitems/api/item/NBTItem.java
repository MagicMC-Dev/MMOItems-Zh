package net.Indyuce.mmoitems.api.item;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public abstract class NBTItem {
	protected ItemStack item;

	public NBTItem(ItemStack item) {
		this.item = item;
	}

	public ItemStack getItem() {
		return item;
	}

	public abstract String getString(String path);

	public abstract boolean hasTag(String path);

	public abstract boolean getBoolean(String path);

	public abstract double getDouble(String path);

	public abstract int getInteger(String path);

	public abstract NBTItem addTag(List<ItemTag> tags);

	public abstract NBTItem removeTag(String... paths);

	public abstract Set<String> getTags();

	public abstract ItemStack toItem();

	// TODO add NBTMeta for display name and lore for less calculations.

	public NBTItem addTag(ItemTag... tags) {
		return addTag(Arrays.asList(tags));
	}

	public double getStat(ItemStat stat) {
		return getStat(stat.getId());
	}

	public double getStat(String stat) {
		return getDouble("MMOITEMS_" + stat);
	}

	public boolean hasType() {
		return hasTag("MMOITEMS_ITEM_TYPE");
	}

	public Type getType() {
		String tag = getString("MMOITEMS_ITEM_TYPE");
		return !tag.equals("") ? MMOItems.plugin.getTypes().get(tag) : null;
	}

	public static NBTItem get(ItemStack item) {
		return MMOItems.plugin.getNMS().getNBTItem(item);
	}
}
