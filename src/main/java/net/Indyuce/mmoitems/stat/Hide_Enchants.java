package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class Hide_Enchants extends BooleanStat {
	public Hide_Enchants() {
		super(new ItemStack(Material.BOOK), "Hide Enchantments", new String[] { "Enable to completely hide your item", "enchants. You can still see the glowing effect." }, "hide-enchants", new String[] { "all" });
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled())
			item.getMeta().addItemFlags(ItemFlag.HIDE_ENCHANTS);
		return true;
	}
	
	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if(item.getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS))
			mmoitem.setData(ItemStat.HIDE_ENCHANTS, new BooleanData(true));
	}
}
