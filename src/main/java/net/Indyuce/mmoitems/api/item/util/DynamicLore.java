package net.Indyuce.mmoitems.api.item.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.LegacyComponent;
import io.lumine.mythic.utils.adventure.text.Component;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.type.DynamicLoreStat;

import java.util.ArrayList;
import java.util.List;

/**
 * Stats like durability and consumable consume amounts need lore updates.
 * Dynamic lore is basically the default item lore with dynamic placeholders
 * marked with % instead of #.
 * <p>
 * Lore with unparsed dynamic placeholders is stored as a JSon array in MMOITEMS_DYNAMIC_LORE.
 * Its placeholders are parsed when using the #apply() method of this class.
 * <p>
 * Any plugin can now register extra stats with dynamic placeholders too
 * with {@link DynamicLoreStat}
 *
 * @author aria rewritten by indyuce
 *
 * @deprecated See {@link LoreUpdate}
 */
@Deprecated
public class DynamicLore {
	private final NBTItem item;

	/**
	 * Constructor used when building an item
	 *
	 * @param item Item being built
	 */
	public DynamicLore(NBTItem item) {
		this.item = item;
	}

	public void update(List<String> lore) {
		for (int j = 0; j < lore.size(); j++)
			lore.set(j, applyDynamicLore(lore.get(j)));
	}

	/**
	 * @param id The stat identifier
	 * @return The corresponding stat, if found, which supports dynamic lore
	 */
	private DynamicLoreStat getCorrespondingStat(String id) {
		for (DynamicLoreStat stat : MMOItems.plugin.getStats().getDynamicLores())
			if (("%" + stat.getDynamicLoreId() + "%").equals(id))
				return stat;
		return null;
	}

	/**
	 * @param input Lore line
	 * @return Line with dynamic placeholders parsed
	 */
	private String applyDynamicLore(String input) {

		// No dynamic lore for this line.
		if (!input.startsWith("%") || !input.endsWith("%"))
			return input;

		/*
		 * External plugins can now register stats with dynamic lore.
		 * This can be used by RPG plugins to have attribute requirements
		 * change color based  on if they are met by the player or not.
		 */
		DynamicLoreStat stat = getCorrespondingStat(input);
		if (stat == null)
			return "<StatNotFound>";

		return stat.calculatePlaceholder(item);
	}

	/**
	 * Apply all the necessary dynamic lore updates for an item
	 */
	public static void update(NBTItem item) {

		// Backwards compatibility for items which do not have dynamic lores
		if (!item.hasTag("MMOITEMS_DYNAMIC_LORE"))
			return;

		DynamicLore dynamic = new DynamicLore(item);
		JsonArray array = MythicLib.plugin.getJson().parse(item.getString("MMOITEMS_DYNAMIC_LORE"), JsonArray.class);
		List<Component> lore = new ArrayList<>(array.size());

		for (JsonElement line : array) {
			if (line == null)
				continue;

			String asString = line.getAsString();
			if (asString == null)
				continue;

			lore.add(LegacyComponent.parse(dynamic.applyDynamicLore(asString)));
		}

		item.setLoreComponents(lore);
	}
}

