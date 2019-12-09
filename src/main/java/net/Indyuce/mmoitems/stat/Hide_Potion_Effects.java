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

public class Hide_Potion_Effects extends BooleanStat {
	public Hide_Potion_Effects() {
		super(new ItemStack(Material.POTION), "Hide Potion Effects", new String[] { "Hides potion effects & 'No Effects'", "from your item lore." }, "hide-potion-effects", new String[] { "all" }, Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW);
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled())
			item.getMeta().addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		return true;
	}
	
	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if(item.getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_POTION_EFFECTS))
			mmoitem.setData(ItemStat.HIDE_POTION_EFFECTS, new BooleanData(true));
	}
}
