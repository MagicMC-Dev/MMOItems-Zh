package net.Indyuce.mmoitems.api.item.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.LegacyComponent;
import io.lumine.mythic.utils.adventure.text.Component;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.type.DynamicLoreStat;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DynamicLore {
	private final NBTItem item;

	public DynamicLore(NBTItem nbt) {
		item = nbt;
	}

	public ItemStack build() {

		if (item.hasTag("MMOITEMS_DYNAMIC_LORE")) {
			JsonArray array = MythicLib.plugin.getJson().parse(item.getString("MMOITEMS_DYNAMIC_LORE"), JsonArray.class);
			List<Component> lore = new ArrayList<>(array.size());

			for (JsonElement line : array)
				lore.add(LegacyComponent.parse(applyDynamicLore(line.getAsString())));

			item.setLoreComponents(lore);
		}
		return item.toItem();
	}

	private DynamicLoreStat getCorrespondingStat(String id) {
		for (DynamicLoreStat stat : MMOItems.plugin.getStats().getDynamicLores())
			if (("%" + stat.getDynamicLoreId() + "%").equals(id))
				return stat;
		return null;
	}

	private String applyDynamicLore(String input) {

		/**
		 * No dynamic lore for this line.
		 */
		if (!input.startsWith("%") || !input.endsWith("%"))
			return input;

		/**
		 * External plugins can now register stats with dynamic lore.
		 * This can be used by RPG plugins to have attribute requirements
		 * change color based  on if they are met by the player or not.
		 */
		DynamicLoreStat stat = getCorrespondingStat(input);
		if (stat == null)
			return "<StatNotFound>";

		return stat.calculatePlaceholder(item);
	}
}
