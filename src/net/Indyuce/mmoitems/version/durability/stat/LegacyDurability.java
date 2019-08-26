package net.Indyuce.mmoitems.version.durability.stat;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class LegacyDurability extends DoubleStat {
	public LegacyDurability() {
		super(new ItemStack(Material.FISHING_ROD), "DefaultDurability/ID", new String[] { "The durability of your item." }, "durability", new String[] { "all" });
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		item.getItemStack().setDurability((short) ((DoubleData) data).generateNewValue());
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.getItem().getItemMeta() instanceof Damageable)
			mmoitem.setData(ItemStat.DURABILITY, new DoubleData(item.getItem().getDurability()));
	}
}
