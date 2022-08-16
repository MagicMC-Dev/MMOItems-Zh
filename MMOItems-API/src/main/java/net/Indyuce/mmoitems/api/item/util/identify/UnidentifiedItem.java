package net.Indyuce.mmoitems.api.item.util.identify;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.item.util.ConfigItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayOutputStream;
import java.util.*;

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
		private final int amount;
		private final NBTItem item;

		private String name = getName();
		private final List<String> lore = new ArrayList<>(getLore());

		public ItemBuilder(NBTItem item) {
			this.amount = item.getItem().getAmount();
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
			int level = mmoitem.hasData(ItemStats.REQUIRED_LEVEL) ? (int) ((DoubleData) mmoitem.getData(ItemStats.REQUIRED_LEVEL)).getValue() : -1;

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
			lore.removeIf(s -> (s.startsWith("{tier}") && tier == null) || (s.startsWith("{range}") && (tier == null || level < 0)));

			/*
			 * apply placeholders
			 */
			for (String placeholder : placeholders.keySet())
				name = name.replace("#" + placeholder + "#", placeholders.get(placeholder));
			for (int n = 0; n < lore.size(); n++) {
				String str = lore.get(n);
				for (String placeholder : placeholders.keySet())
					str = str.replace("#" + placeholder + "#", placeholders.get(placeholder));
				lore.set(n, MythicLib.plugin.parseColors(str.replace("{range}", "").replace("{tier}", "")));
			}

			/*
			 * apply changes to item
			 */
			item.getItem().setAmount(1);
			ItemStack unidentified = MythicLib.plugin.getVersion().getWrapper().copyTexture(item)
					.addTag(new ItemTag("MMOITEMS_UNIDENTIFIED_ITEM", serialize(item.toItem()))).toItem();
			unidentified.setAmount(amount);
			ItemMeta meta = unidentified.getItemMeta();
			meta.addItemFlags(ItemFlag.values());
			meta.setUnbreakable(true);
			meta.setDisplayName(MythicLib.plugin.parseColors(name));
			meta.setLore(lore);
			if (customModelData != null) { meta.setCustomModelData(customModelData);}
			unidentified.setItemMeta(meta);

			// Has model?
			if (material != null && material.isItem()) { unidentified.setType(material); }

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
