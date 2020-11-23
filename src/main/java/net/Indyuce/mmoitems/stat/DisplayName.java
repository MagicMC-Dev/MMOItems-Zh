package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.version.VersionMaterial;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public class DisplayName extends StringStat {
	public DisplayName() {
		super("NAME", new ItemStack(VersionMaterial.OAK_SIGN.toMaterial()), "Display Name", new String[] { "The item display name." },
				new String[] { "all" });
	}

	@Override
	public void whenApplied(ItemStackBuilder item, StatData data) {
		String format = data.toString();

		ItemTier tier = MMOItems.plugin.getTiers().findTier(item.getMMOItem());
		format = format.replace("<tier-name>", tier != null ? ChatColor.stripColor(tier.getName()) : "");
		format = format.replace("<tier-color>", tier != null ? ChatColor.getLastColors(tier.getName()) : "&f");

		item.getMeta().setDisplayName(MMOLib.plugin.parseColors(format));
	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().getItem().getItemMeta().hasDisplayName())
			mmoitem.setData(ItemStats.NAME, new StringData(mmoitem.getNBT().getItem().getItemMeta().getDisplayName()));
	}
}
