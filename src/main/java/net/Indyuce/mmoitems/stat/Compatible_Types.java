package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.version.VersionMaterial;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Compatible_Types extends StringStat {
	public Compatible_Types() {
		super(new ItemStack(VersionMaterial.COMMAND_BLOCK.toMaterial()), "Compatible Types", new String[] { "The item types this skin is", "compatible with." }, "compatible-types", new String[] { "skin" });
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.COMPATIBLE_TYPES).enable("Write in the chat the name of the type you want to add.");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("compatible-types")) {
				List<String> lore = config.getConfig().getStringList(inv.getItemId() + ".compatible-types");
				if (lore.size() < 1)
					return true;

				String last = lore.get(lore.size() - 1);
				lore.remove(last);
				config.getConfig().set(inv.getItemId() + ".compatible-types", lore);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed '" + ChatColor.translateAlternateColorCodes('&', last) + ChatColor.GRAY + "'.");
			}
		}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		List<String> lore = config.getConfig().getConfigurationSection(inv.getItemId()).contains("compatible-types") ? config.getConfig().getStringList(inv.getItemId() + ".compatible-types") : new ArrayList<>();
		lore.add(message.toUpperCase());
		config.getConfig().set(inv.getItemId() + ".compatible-types", lore);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Compatible Types successfully added.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("compatible-types"))
			lore.add(ChatColor.RED + "No compatible types.");
		else if (config.getStringList(path + ".compatible-types").isEmpty())
			lore.add(ChatColor.RED + "No compatible types.");
		else
			config.getStringList(path + ".compatible-types").forEach(str -> lore.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', str)));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a new type.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last type.");
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		item.setData(ItemStat.COMPATIBLE_TYPES, new StringListData(config.getStringList("compatible-types")));
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		List<String> compatibleTypes = new ArrayList<>();
		JsonArray array = new JsonArray();
		((StringListData) data).getList().forEach(line -> {
			line = ChatColor.translateAlternateColorCodes('&', line);
			array.add(line);
			compatibleTypes.add(line);
		});
		item.getLore().insert("compatible-types", compatibleTypes);
		item.addItemTag(new ItemTag("MMOITEMS_COMPATIBLE_TYPES", array.toString()));
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
		if (item.hasTag("MMOITEMS_COMPATIBLE_TYPES"))
			mmoitem.setData(ItemStat.COMPATIBLE_TYPES, new StringListData(new JsonParser().parse(item.getString("MMOITEMS_COMPATIBLE_TYPES")).getAsJsonArray()));
	}
}
