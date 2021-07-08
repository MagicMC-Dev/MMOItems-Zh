package net.Indyuce.mmoitems.api.item.build;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * There are three types of lore placeholders.
 * <p>
 * Most placeholders are like #attack-damage#, they are
 * called static placeholders.
 * <p>
 * Special placeholders are {placeholder-name}, they can be used
 * in the item description, the one you get with {@link net.Indyuce.mmoitems.stat.Lore}
 * <p>
 * Dynamic placeholders are %placeholder-name%, they
 * are used by custom durability, consumable uses left, etc.
 *
 * @author indyuce
 */
public class LoreBuilder {
	private final List<String> lore = new ArrayList<>();
	private final List<String> end = new ArrayList<>();
	private final Map<String, String> placeholders = new HashMap<>();

	/**
	 * Default constructor used when building items
	 *
	 * @param format
	 */
	public LoreBuilder(Collection<String> format) {
		lore.addAll(format);
	}

	/**
	 * Inserts a list of strings in the item lore. The lines are added only if a
	 * line #item-stat-id# can be found in the lore format.
	 *
	 * @param path The path of the stat, used to locate where to insert the stat
	 *             in the lore
	 * @param add  The lines you want to add
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
	 * @param path The path of the stat, used to locate where to insert the stat
	 *             in the lore
	 * @param list The lines you want to add
	 */
	public void insert(String path, List<String> list) {
		int index = lore.indexOf("#" + path + "#");
		if (index < 0)
			return;

		Lists.reverse(list).forEach(string -> lore.add(index + 1, string));
		lore.remove(index);
	}

	/**
	 * Registers a placeholder. All placeholders registered will be parsed when
	 * using applyLorePlaceholders(String)
	 *
	 * @param path  The placeholder path (CASE SENSITIVE)
	 * @param value The placeholder value which is instantly saved as a string
	 *              when registered
	 */
	public void registerPlaceholder(String path, Object value) {
		placeholders.put(path, value.toString());
	}

	/**
	 * Parses a string with registered special placeholders
	 *
	 * @param str String with {..} unformatted placeholders
	 * @return Same string with replaced placeholders. Placeholders which
	 * couldn't be found are marked with PHE which means
	 * PlaceHolderError
	 */
	public String applySpecialPlaceholders(String str) {

		while (str.contains("{") && str.substring(str.indexOf("{")).contains("}")) {
			String holder = str.substring(str.indexOf("{") + 1, str.indexOf("}"));
			str = str.replace("{" + holder + "}", placeholders.getOrDefault(holder, "PHE"));
		}

		return str;
	}

	/**
	 * Adds a line of lore at the end of it
	 *
	 * @param str String to insert at the end
	 */
	public void end(@NotNull String str) {
		end.add(str);
	}

	/**
	 * @return A built item lore. This method must be called after all lines
	 * have been inserted in the lore. It cleans all unused static placeholders
	 * as well as lore bars. The dynamic placeholders still remain however.
	 */
	public List<String> build() {

		/*
		 * Loops backwards to remove all unused bars in one iteration only,
		 * otherwise the stats under a bar gets removed after the bar is checked
		 */
		for (int j = 0; j < lore.size(); ) {
			int n = lore.size() - j - 1;
			String line = lore.get(n);

			// Remove unused static lore placeholders
			if (line.startsWith("#"))
				lore.remove(n);

				// Remove useless lore stripes
			else if (line.startsWith("{bar}") && (n == lore.size() - 1 || isBar(lore.get(n + 1))))
				lore.remove(n);

			else
				j++;
		}

		/*
		 * Clear bar codes and parse chat colors only ONCE the bars have been
		 * successfully calculated. Also breaks lines containing \n (like breaks)
		 *
		 * Edit so that there is no need to create an additional array list
		 */
		for (int j = 0; j < lore.size(); ) {

			// Apply color codes and replace bar prefixes
			String str = MythicLib.plugin.parseColors(lore.get(j).replace("{bar}", "").replace("{sbar}", ""));

			// Need to break down the line into multiple
			if (str.contains("\\n")) {
				lore.remove(j);

				String[] split = str.split("\\\\n");
				for (int k = split.length - 1; k >= 0; k -= 1)
					lore.add(j + 1, split[k]);

				j += split.length;

				// Simple line
			} else
				lore.set(j++, str);
		}

		lore.addAll(end);
		return lore;
	}

	private boolean isBar(String str) {
		return str.startsWith("{bar}") || str.startsWith("{sbar}");
	}
}
