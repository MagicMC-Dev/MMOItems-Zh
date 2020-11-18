package net.Indyuce.mmoitems.api.item.build;

import com.google.common.collect.Lists;
import net.mmogroup.mmolib.MMOLib;

import java.util.*;

public class LoreBuilder {
	private final List<String> lore = new ArrayList<>();
	private final Map<String, String> placeholders = new HashMap<>();

	public LoreBuilder(Collection<String> format) {
		lore.addAll(format);
	}

	/**
	 * Registers a placeholder. All placeholders registered will be parsed when
	 * using applyLorePlaceholders(String)
	 * 
	 * @param path
	 *            The placeholder path (CASE SENSITIVE)
	 * @param value
	 *            The placeholder value which is instantly saved as a string
	 *            when registered
	 */
	public void registerPlaceholder(String path, Object value) {
		placeholders.put(path, value.toString());
	}

	/**
	 * Parses a string with registered placeholders
	 * 
	 * @param str
	 *            String with {..} unformatted placeholders
	 * @return Same string with replaced placeholders. Placeholders which
	 *         couldn't be found are marked with PHE which means
	 *         PlaceHolderError
	 */
	public String applyLorePlaceholders(String str) {

		while (str.contains("{") && str.substring(str.indexOf("{")).contains("}")) {
			String holder = str.substring(str.indexOf("{") + 1, str.indexOf("}"));
			str = str.replace("{" + holder + "}", placeholders.containsKey(holder) ? placeholders.get(holder) : "PHE");
		}

		return MMOLib.plugin.parseColors(str);
	}

	/**
	 * Inserts a list of strings in the item lore. The lines are added only if a
	 * line #item-stat-id# can be found in the lore format.
	 * 
	 * @param path
	 *            The path of the stat, used to locate where to insert the stat
	 *            in the lore
	 * @param add
	 *            The lines you want to add
	 */
	public void insert(String path, String... add) {
		int index = lore.indexOf("#" + path + "#");
		if (index < 0)
			return;

		for (int j = 0; j < add.length; j++)
			lore.add(index + 1, add[add.length - j - 1]);
		lore.remove(index);
	}

	/**
	 * Inserts a list of strings in the item lore. The lines are added only if a
	 * line #item-stat-id# can be found in the lore format.
	 * 
	 * @param path
	 *            The path of the stat, used to locate where to insert the stat
	 *            in the lore
	 * @param list
	 *            The lines you want to add
	 */
	public void insert(String path, List<String> list) {
		int index = lore.indexOf("#" + path + "#");
		if (index < 0)
			return;

		Lists.reverse(list).forEach(string -> lore.add(index + 1, string));
		lore.remove(index);
	}

	/**
	 * @return A built item lore. This method must be called after all lines
	 *         have been inserted in the lore. It cleans all unused lore format
	 *         # lines as well as lore bars
	 */
	public List<String> build() {

		/*
		 * loops backwards to remove all unused bars in one iteration only,
		 * otherwise the stats under a bar gets removed after the bar is checked
		 */
		for (int j = 0; j < lore.size();) {
			int n = lore.size() - j - 1;
			String line = lore.get(n);

			// removed unused placeholders
			if (line.startsWith("#"))
				lore.remove(n);

			// remove useless lore stripes
			else if (line.startsWith("{bar}") && (n == lore.size() - 1 || isBar(lore.get(n + 1))))
				lore.remove(n);

			else
				j++;
		}

		/*
		 * clear bar codes and parse chat colors only ONCE the bars have been
		 * successfully calculated
		 * 
		 * NEW: also finalize the lore by breaking lines with the \n escape
		 * character
		 */
		final List<String> cleaned = new ArrayList<>();
		for (int i = 0; i < lore.size(); i++)
			for (final String s : MMOLib.plugin.parseColors(lore.get(i).replace("{bar}", "").replace("{sbar}", "")).split("\\\\n"))
				cleaned.add(s);

		return cleaned;
	}

	private boolean isBar(String str) {
		return str.startsWith("{bar}") || str.startsWith("{sbar}");
	}
}
