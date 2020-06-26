package net.Indyuce.mmoitems.api.item.plugin.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.api.crafting.CraftingStatus.CraftingQueue.CraftingInfo;
import net.Indyuce.mmoitems.api.crafting.recipe.CraftingRecipe;
import net.Indyuce.mmoitems.api.item.plugin.ConfigItem;
import net.asangarin.hexcolors.ColorParse;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;

public class QueueItemDisplay extends ConfigItem {
	private static final long[] ms = { 1000, 60 * 1000, 60 * 60 * 1000, 24 * 60 * 60 * 1000 };
	private static final String[] chars = { "s", "m", "h", "d" };

	public QueueItemDisplay() {
		super("QUEUE_ITEM_DISPLAY", Material.BARRIER, "&6&lQueue&f #name#", "{ready}&7&oThis item was successfully crafted.", "{queue}&7&oThis item is in the crafting queue.", "{queue}", "{queue}&7Time Left: &c#left#", "", "{ready}&eClick to claim!", "{queue}&eClick to cancel.");
	}

	public ItemBuilder newBuilder(CraftingInfo crafting, int position) {
		return new ItemBuilder(crafting, position);
	}

	public class ItemBuilder {
		private final CraftingInfo crafting;
		private final int position;

		private String name = new String(getName());
		private List<String> lore = new ArrayList<>(getLore());

		public ItemBuilder(CraftingInfo crafting, int position) {
			this.crafting = crafting;
			this.position = position;
		}

		public ItemStack build() {
			Map<String, String> replace = new HashMap<>();

			for (Iterator<String> iterator = lore.iterator(); iterator.hasNext();) {
				String str = iterator.next();

				/*
				 * crafting time
				 */
				if (str.startsWith("{queue}")) {
					if (crafting.isReady()) {
						iterator.remove();
						continue;
					}

					replace.put(str, str.replace("{queue}", ""));
				}

				if (str.startsWith("{ready}")) {
					if (!crafting.isReady()) {
						iterator.remove();
						continue;
					}

					replace.put(str, str.replace("{ready}", ""));
				}
			}

			for (String key : replace.keySet())
				lore.set(lore.indexOf(key), replace.get(key).replace("#left#", formatDelay(crafting.getLeft(), 2)));

			/*
			 * apply color to lore
			 */
			for (int n = 0; n < lore.size(); n++)
				lore.set(n, new ColorParse('&', lore.get(n)).toChatColor());

			ItemStack item = ((CraftingRecipe) crafting.getRecipe()).getOutput().getPreview();
			item.setAmount(position);
			ItemMeta meta = item.getItemMeta();
			meta.addItemFlags(ItemFlag.values());
			meta.setDisplayName(new ColorParse('&', name.replace("#name#", meta.getDisplayName())).toChatColor());
			meta.setLore(lore);
			item.setItemMeta(meta);

			return MMOLib.plugin.getNMS().getNBTItem(item).addTag(new ItemTag("queueId", crafting.getUniqueId().toString())).toItem();
		}
	}

	private String formatDelay(long delay, int max) {
		String format = "";

		int n = 0;
		for (int j = ms.length - 1; j >= 0 && n < max; j--)
			if (delay >= ms[j]) {
				format += (delay / ms[j]) + chars[j] + " ";
				delay = delay % ms[j];
				n++;
			}

		return format.isEmpty() ? "1s" : format;
	}
}
