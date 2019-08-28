package net.Indyuce.mmoitems.api.item.build;

import java.util.List;

import org.bukkit.ChatColor;

import com.google.common.collect.Lists;

import net.Indyuce.mmoitems.MMOItems;

public class MMOItemLore {
	private final List<String> lore = MMOItems.plugin.getLanguage().getDefaultLoreFormat();

	public void insert(String path, String... add) {
		int index = lore.indexOf("#" + path + "#");
		if (index < 0)
			return;

		for (int j = 0; j < add.length; j++)
			lore.add(index + 1, add[add.length - j - 1]);
		lore.remove(index);
	}

	public void insert(String path, List<String> list) {
		int index = lore.indexOf("#" + path + "#");
		if (index < 0)
			return;

		Lists.reverse(list).forEach(string -> lore.add(index + 1, string));
		lore.remove(index);
	}

	public MMOItemLore build() {

		/*
		 * loops backwards to remove all unused bars in one iteration only,
		 * otherwise the stats under a bar gets removed after the bar is checked
		 */
		for (int j = 0; j < lore.size();) {
			int n = lore.size() - j - 1;
			String line = lore.get(n);

			// removed unused placeholders
			if (line.startsWith("#")) {
				lore.remove(n);
				continue;
			}

			// remove useless lore stripes
			if (line.startsWith("{bar}") && (n == lore.size() - 1 || isBar(lore.get(n + 1)))) {
				lore.remove(n);
				continue;
			}

			j++;
		}

		/*
		 * clear bar codes and chat colors only ONCE the bars have been
		 * successfully calculated
		 */
		for (int n = 0; n < lore.size(); n++)
			lore.set(n, ChatColor.translateAlternateColorCodes('&', lore.get(n).replace("{bar}", "").replace("{sbar}", "")));

		return this;
	}

	private boolean isBar(String str) {
		return str.startsWith("{bar}") || str.startsWith("{sbar}");
	}

	public List<String> toStringList() {
		return lore;
	}
}
