package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;

public class HideDye extends BooleanStat {
	public HideDye() {
		super("HIDE_DYE", Material.CYAN_DYE, "Hide Dyed", new String[] { "Enable to hide the 'Dyed' tag from the item." }, new String[] { "all" },
			Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS, VersionMaterial.LEATHER_HORSE_ARMOR.toMaterial());

		try {
			ItemFlag.valueOf("HIDE_DYE");
		} catch(IllegalArgumentException exception) {
			disable();
		}
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		if (((BooleanData) data).isEnabled())
			item.getMeta().addItemFlags(ItemFlag.HIDE_DYE);
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_DYE))
			mmoitem.setData(ItemStats.HIDE_DYE, new BooleanData(true));
	}
}
