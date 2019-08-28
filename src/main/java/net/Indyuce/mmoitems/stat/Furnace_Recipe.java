package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;

public class Furnace_Recipe extends StringStat {
	public Furnace_Recipe() {
		super(new ItemStack(Material.FURNACE), "Furnace Recipe", new String[] { "Defines what item you need", "to smelt to get your item.", "Changing this value requires a reload." }, "furnace-craft", new String[] { "all" });
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.FURNACE_RECIPE, "").enable("Write in the chat the material you want to be the ingredient.");

		if (event.getAction() == InventoryAction.PICKUP_HALF)
			new StatEdition(inv, ItemStat.FURNACE_RECIPE, "exp").enable("Write in the chat the experience you want your recipe to generate.");

		if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)
			new StatEdition(inv, ItemStat.FURNACE_RECIPE, "cook").enable("Write in the chat the recipe cooking time.");

		if (event.getAction() == InventoryAction.DROP_ONE_CURSOR)
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("furnace-craft")) {
				config.getConfig().set(inv.getItemId() + ".furnace-craft", null);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed the furnace recipe.");
			}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String edited = (String) info[0];

		// set furnace recipe experience
		if (edited.equals("exp")) {
			float exp = 0;
			try {
				exp = (float) Double.parseDouble(message);
			} catch (Exception e) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid number!");
				return false;
			}

			config.getConfig().set(inv.getItemId() + ".furnace-craft.exp", exp);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Furnace recipe experience successfully set to " + ChatColor.GOLD + exp + ChatColor.GRAY + ".");
			return true;
		}

		// set furnace recipe cooking time
		if (edited.equals("cook")) {
			int cook = 0;
			try {
				cook = Integer.parseInt(message);
			} catch (Exception e) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + message + " is not a valid integer!");
				return false;
			}

			config.getConfig().set(inv.getItemId() + ".furnace-craft.cook", cook);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Furnace recipe experience successfully changed to " + ChatColor.GOLD + cook + ChatColor.GRAY + ".");
			return true;
		}

		Material material = null;
		String format = message.toUpperCase().replace("-", "_").replace(" ", "_");
		try {
			material = Material.valueOf(format);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + format + " is not a valid material!");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "All materials can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");
			return false;
		}

		config.getConfig().set(inv.getItemId() + ".furnace-craft.input", format);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GOLD + MMOUtils.caseOnWords(material.name().replace("_", " ").toLowerCase()) + ChatColor.GRAY + " successfully set as the furnace recipe ingredient.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("furnace-craft"))
			lore.add(ChatColor.RED + "No furnace recipe.");
		else {
			lore.add(ChatColor.GRAY + "* Input: " + ChatColor.GREEN + config.getString(path + ".furnace-craft.input"));
			lore.add(ChatColor.GRAY + "* Experience: " + ChatColor.GREEN + config.getDouble(path + ".furnace-craft.exp"));
			lore.add(ChatColor.GRAY + "* Cooking Time: " + ChatColor.GREEN + config.getInt(path + ".furnace-craft.cook") + ChatColor.GRAY + " ticks");
		}
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to set the ingredient.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to set the experience output.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Shift click to set the cooking time.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Drop to remove the recipe.");
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		return true;
	}
}
