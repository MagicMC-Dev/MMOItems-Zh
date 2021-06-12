package net.Indyuce.mmoitems.api.item.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.LegacyComponent;
import io.lumine.utils.adventure.text.Component;
import net.Indyuce.mmoitems.MMOItems;

public class DynamicLore {
	private final NBTItem item;
	
	public DynamicLore(NBTItem nbt) {
		item = nbt;
	}
	
	public ItemStack build() {
		if (item.hasTag("MMOITEMS_DYNAMIC_LORE")) {
			JsonArray array = MythicLib.plugin.getJson().parse(item.getString("MMOITEMS_DYNAMIC_LORE"), JsonArray.class);
			List<Component> lore = new ArrayList<>(array.size());
			for (JsonElement e : array) {
				String s = replace(e.getAsString());
				if(!s.equals("!INVALID!"))
					lore.add(LegacyComponent.parse(s));
			}
			item.setLoreComponents(lore);
		}
		return item.toItem();
	}
	
	private String replace(String input) {
		//noinspection SwitchStatementWithTooFewBranches
		switch(input.toLowerCase()) {
			case "%durability%":
				if(item.hasTag("MMOITEMS_DURABILITY") && item.hasTag("MMOITEMS_MAX_DURABILITY"))
					return (MMOItems.plugin.getLanguage().getDynLoreFormat("durability")
					.replace("%durability%", "" + item.getInteger("MMOITEMS_DURABILITY"))
					.replace("%max_durability%", "" + item.getInteger("MMOITEMS_MAX_DURABILITY")));
				else return "!INVALID!";
			default:
				return input;
		}
	}
}
