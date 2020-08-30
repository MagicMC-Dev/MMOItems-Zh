package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
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
		item.getMeta().setDisplayName(new DisplayNamePlaceholders(data.toString(), item.getMMOItem()).parse());

	}

	@Override
	public void whenLoaded(ReadMMOItem mmoitem) {
		if (mmoitem.getNBT().getItem().getItemMeta().hasDisplayName())
			mmoitem.setData(ItemStat.NAME, new StringData(mmoitem.getNBT().getItem().getItemMeta().getDisplayName()));
	}

	private class DisplayNamePlaceholders {

		private String name;

		private final MMOItem mmoitem;

		private DisplayNamePlaceholders(String name, MMOItem mmoitem) {
			this.name = name;
			this.mmoitem = mmoitem;
		}

		private String parse() {
			name = name.replace("<tier-name>", (mmoitem.hasData(ItemStat.TIER))
					? stripColorCodes(MMOItems.plugin.getTiers().findTier(mmoitem).getName()) : "");
			name = name.replace("<tier-color>", (mmoitem.hasData(ItemStat.TIER))
					?  stripText(MMOItems.plugin.getTiers().findTier(mmoitem).getName()) : "&f");
			name = name.replace("<type-name>", (mmoitem.hasData(ItemStat.DISPLAYED_TYPE))
					?  stripColorCodes(mmoitem.getData(ItemStat.DISPLAYED_TYPE).toString()) : stripColorCodes(mmoitem.getType().getName()));
			return MMOLib.plugin.parseColors(name);
		}

		private String stripColorCodes(String message) {
			return ChatColor.stripColor(MMOLib.plugin.parseColors(message));
		}

		private String stripText(String message) {
			return ChatColor.getLastColors(MMOLib.plugin.parseColors(message));
		}
	}

}
