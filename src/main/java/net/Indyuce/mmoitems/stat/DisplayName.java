package net.Indyuce.mmoitems.stat;

import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.version.VersionMaterial;

public class DisplayName extends StringStat {
	public DisplayName() {
		super("NAME", new ItemStack(VersionMaterial.OAK_SIGN.toMaterial()), "Display Name", new String[] { "The item display name." },
				new String[] { "all" });
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		item.getMeta().setDisplayName(MMOLib.plugin.parseColors(data.toString()));
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().getItem().getItemMeta().hasDisplayName())
			mmoitem.setData(ItemStat.NAME, new StringData(mmoitem.getNBT().getItem().getItemMeta().getDisplayName()));
	}
}
