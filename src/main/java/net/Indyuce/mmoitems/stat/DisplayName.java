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
			(mmoitem).setData(ItemStat.NAME, new StringData(mmoitem.getNBT().getItem().getItemMeta().getDisplayName()));
	}

	/*
	 * When loading display names, Spigot does not register white color codes
	 * when they are placed first in the item name. If the name starts with the
	 * white color code, just add an extra color code which won't be seen on the
	 * item
	 */
	// @Deprecated
	// private String fix(String str) {
	// return str.startsWith(ChatColor.WHITE + "") ? "" + ChatColor.GREEN +
	// ChatColor.WHITE + str : str;
	// }

	// public String getDisplayName(String display) {
	//
	// // name placeholders
	// String[] split = display.split("\\<");
	// if (split.length > 1)
	// // starting at 0 is pointless
	// for (int j = 1; j < split.length; j++) {
	// String jstr = split[j];
	// if (!jstr.contains(">"))
	// continue;
	//
	// String ref = jstr.split("\\>")[0];
	// String placeholder =
	// MMOItems.plugin.getLanguage().getNamePlaceholder(ref);
	// if (placeholder != null)
	// display = display.replace("<" + ref + ">", placeholder);
	// }
	//
	// return display;
	// }
}
