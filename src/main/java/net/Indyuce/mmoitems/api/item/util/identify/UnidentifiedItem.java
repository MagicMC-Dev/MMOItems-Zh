package net.Indyuce.mmoitems.api.item.util.identify;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.item.util.ConfigItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;

public class UnidentifiedItem extends ConfigItem {
	public UnidentifiedItem(Type type) {
		super("unidentified", type.getItem().getType());

		setName("#prefix#Unidentified " + type.getName());
		setLore(Arrays.asList("&7This item is unidentified. I must", "&7find a way to identify it!", "{tier}", "{tier}&8Item Info:",
				"{range}&8- &7Lvl Range: &e#range#", "{tier}&8- &7Item Tier: #prefix##tier#"));
	}

	public ItemBuilder newBuilder(NBTItem item) {
		return new ItemBuilder(item);
	}

	/*
	 * allows to build an unidentified item based on the given NBTItem.
	 */
	public class ItemBuilder {
		private final NBTItem item;

		private String name = new String(getName());
		private List<String> lore = new ArrayList<>(getLore());

		public ItemBuilder(NBTItem item) {
			this.item = item;
		}

		// {tier} only displays when tier
		// {level} only displays when level + tier
		public ItemStack build() {

			/*
			 * load item data
			 */
			MMOItem mmoitem = new VolatileMMOItem(item);
			ItemTier tier = MMOItems.plugin.getTiers().findTier(mmoitem);
			int level = mmoitem.hasData(ItemStat.REQUIRED_LEVEL) ? (int) ((DoubleData) mmoitem.getData(ItemStat.REQUIRED_LEVEL)).getValue() : -1;

			/*
			 * load placeholders
			 */
			Map<String, String> placeholders = new HashMap<>();
			if (tier != null) {
				placeholders.put("prefix", tier.getUnidentificationInfo().getPrefix());
				placeholders.put("tier", tier.getUnidentificationInfo().getDisplayName());

				if (level > -1) {
					int[] range = tier.getUnidentificationInfo().calculateRange(level);
					placeholders.put("range", range[0] + "-" + range[1]);
				}
			} else
				name = name.replace("#prefix#", "");

			/*
			 * remove useless lore lines
			 */
			for (Iterator<String> iterator = lore.iterator(); iterator.hasNext();) {
				String next = iterator.next();
				if ((next.startsWith("{tier}") && tier == null) || (next.startsWith("{range}") && (tier == null || level < 0)))
					iterator.remove();
			}

			/*
			 * apply placeholders
			 */
			for (String placeholder : placeholders.keySet())
				name = name.replace("#" + placeholder + "#", placeholders.get(placeholder));
			for (int n = 0; n < lore.size(); n++) {
				String str = lore.get(n);
				for (String placeholder : placeholders.keySet())
					str = str.replace("#" + placeholder + "#", placeholders.get(placeholder));
				lore.set(n, MMOLib.plugin.parseColors(str.replace("{range}", "").replace("{tier}", "")));
			}

			/*
			 * apply changes to item
			 */
			ItemStack unidentified = MMOLib.plugin.getVersion().getWrapper().copyTexture(item)
					.addTag(new ItemTag("MMOITEMS_UNIDENTIFIED_ITEM", serialize(item.getItem()))).toItem();
			ItemMeta meta = unidentified.getItemMeta();
			meta.addItemFlags(ItemFlag.values());
			meta.setUnbreakable(true);
			meta.setDisplayName(MMOLib.plugin.parseColors(name));
			meta.setLore(lore);
			unidentified.setItemMeta(meta);

			return unidentified;
		}

		private String serialize(ItemStack item) {
			try {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
				dataOutput.writeObject(item);
				dataOutput.close();
				return Base64Coder.encodeLines(outputStream.toByteArray());
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
	}
}
