package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.ReadMMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class HidePotionEffects extends BooleanStat {
	public HidePotionEffects() {
		super("HIDE_POTION_EFFECTS", new ItemStack(Material.POTION), "Hide Potion Effects", new String[] { "Hides potion effects & 'No Effects'", "from your item lore." }, new String[] { "all" }, Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW);
	}

	@Override
	public void whenApplied(MMOItemBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled())
			item.getMeta().addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_POTION_EFFECTS))
			mmoitem.setData(ItemStat.HIDE_POTION_EFFECTS, new BooleanData(true));
	}
}
