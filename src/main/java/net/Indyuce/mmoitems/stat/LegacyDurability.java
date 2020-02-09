package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.entity.Damageable;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.NBTItem;

public class LegacyDurability extends DoubleStat {
	public LegacyDurability() {
		super(new ItemStack(Material.FISHING_ROD), "Item Damage/ID", new String[] { "The durability/ID of your item. This", "does &cNOT&7 impact the item max durability." }, "durability", new String[] { "all" });
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
