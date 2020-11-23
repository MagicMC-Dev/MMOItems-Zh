package net.Indyuce.mmoitems.api.item.util.crafting;

import net.Indyuce.mmoitems.api.crafting.CraftingStatus.CraftingQueue.CraftingInfo;
import net.Indyuce.mmoitems.api.item.util.ConfigItem;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class QueueItemDisplay extends ConfigItem {
	private static final long[] ms = { 1000, 60 * 1000, 60 * 60 * 1000, 24 * 60 * 60 * 1000 };
	private static final String[] chars = { "s", "m", "h", "d" };

	public QueueItemDisplay() {
		super("QUEUE_ITEM_DISPLAY", Material.BARRIER, "&6&lQueue&f #name#", "{ready}&7&oThis item was successfully crafted.",
				"{queue}&7&oThis item is in the crafting queue.", "{queue}", "{queue}&7Time Left: &c#left#", "", "{ready}&eClick to claim!",
				"{queue}&eClick to cancel.");
	}

	public ItemBuilder newBuilder(CraftingInfo crafting, int position) {
		return new ItemBuilder(crafting, position);
	}

	public class ItemBuilder {
		private final CraftingInfo crafting;
		private final int position;

		private final String name = getName();
		private final List<String> lore = new ArrayList<>(getLore());

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
				lore.set(lore.indexOf(key), replace.get(key).replace("#left#", formatDelay(crafting.getLeft())));

			/*
			 * apply color to lore
			 */
			for (int n = 0; n < lore.size(); n++)
				lore.set(n, MMOLib.plugin.parseColors(lore.get(n)));

			ItemStack item = crafting.getRecipe().getOutput().getPreview();
			item.setAmount(position);
			ItemMeta meta = item.getItemMeta();
			meta.addItemFlags(ItemFlag.values());
			meta.setDisplayName(MMOLib.plugin.parseColors(name.replace("#name#", meta.getDisplayName())));
			meta.setLore(lore);
			item.setItemMeta(meta);

			return MMOLib.plugin.getVersion().getWrapper().getNBTItem(item).addTag(new ItemTag("queueId", crafting.getUniqueId().toString()))
					.toItem();
		}
	}

	private String formatDelay(long delay) {
		StringBuilder format = new StringBuilder();

		int n = 0;
		for (int j = ms.length - 1; j >= 0 && n < 2; j--)
			if (delay >= ms[j]) {
				format.append(delay / ms[j]).append(chars[j]).append(" ");
				delay = delay % ms[j];
				n++;
			}

		return (format.length() == 0) ? "1s" : format.toString();
	}
}
