package net.Indyuce.mmoitems.stat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Enums;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.version.VersionMaterial;

public class HideDye extends BooleanStat {
	public HideDye() {
		super("HIDE_DYE", new ItemStack(Material.CYAN_DYE), "Hide Dyed", new String[] { "Enable to hide the 'Dyed' tag from the item." }, new String[] { "all" },
			Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, VersionMaterial.LEATHER_HORSE_ARMOR.toMaterial());

		if (!Enums.getIfPresent(ItemFlag.class, "HIDE_DYE").isPresent())
			disable();
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled())
			item.getMeta().addItemFlags(ItemFlag.HIDE_DYE);
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_DYE))
			mmoitem.setData(ItemStat.HIDE_DYE, new BooleanData(true));
	}
}
