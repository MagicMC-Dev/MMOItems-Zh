package net.Indyuce.mmoitems.api.item.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class DynamicLore {
	private final NBTItem item;
	
	public DynamicLore(NBTItem nbt) {
		item = nbt;
	}
	
	public ItemStack build() {
		ItemStack stack = item.toItem();
		if (item.hasTag("MMOITEMS_DYNAMIC_LORE")) {
			JsonArray array = MythicLib.plugin.getJson().parse(item.getString("MMOITEMS_DYNAMIC_LORE"), JsonArray.class);
			List<String> lore = new ArrayList<>();
			for (JsonElement e : array) {
				String s = replace(e.getAsString());
				if(!s.equals("!INVALID!"))
					lore.add(s);
			}
			ItemMeta meta = stack.getItemMeta();
			meta.setLore(lore);
			stack.setItemMeta(meta);

		}
		NBTItem nbt = NBTItem.get(stack);
		if (nbt.getLoreComponents() != null) {
			nbt.setLoreComponents(MythicLib.plugin.getComponentBuilder().parse(nbt.getLoreComponents()));
		}
		return nbt.toItem();
	}
	
	private String replace(String input) {
		//noinspection SwitchStatementWithTooFewBranches
		switch(input.toLowerCase()) {
			case "%durability%":
				if(item.hasTag("MMOITEMS_DURABILITY") && item.hasTag("MMOITEMS_MAX_DURABILITY"))
					return MythicLib.plugin.parseColors(MMOItems.plugin.getLanguage().getDynLoreFormat("durability")
					.replace("%durability%", "" + item.getInteger("MMOITEMS_DURABILITY"))
					.replace("%max_durability%", "" + item.getInteger("MMOITEMS_MAX_DURABILITY")));
				else return "!INVALID!";
			default:
				return input;
		}
	}
}
