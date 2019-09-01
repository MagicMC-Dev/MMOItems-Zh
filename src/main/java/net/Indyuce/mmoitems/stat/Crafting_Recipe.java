package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.RecipeEdition;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.version.VersionMaterial;

public class Crafting_Recipe extends StringStat {
	public Crafting_Recipe() {
		super(new ItemStack(VersionMaterial.CRAFTING_TABLE.toMaterial()), "Crafting Recipe", new String[] { "The recipe of your item.", "Changing this value requires a reload." }, "craft", new String[] { "all" });
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new RecipeEdition(inv.getPlayer(), inv.getItemType(), inv.getItemId()).open(inv.getPage());
		if (event.getAction() == InventoryAction.PICKUP_HALF)
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("craft")) {
				config.getConfig().set(inv.getItemId() + ".craft", null);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Crafting Recipe successfully removed.");
			}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		Material material = null;
		int durability = 0;
		String[] split = message.toUpperCase().replace("-", "_").replace(" ", "_").split("\\:");
		try {
			material = Material.valueOf(split[0]);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[0] + " is not a valid material!");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "All materials can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");
			return false;
		}
		if (split.length > 1)
			try {
				durability = Integer.parseInt(split[1]);
			} catch (Exception e1) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + split[1] + " is not a valid number!");
				return false;
			}

		int n = (int) info[0];
		List<String> list = new ArrayList<>();
		if (config.getConfig().getConfigurationSection(inv.getItemId()).getKeys(false).contains("craft"))
			list = config.getConfig().getStringList(inv.getItemId() + ".craft");
		while (list.size() < 3)
			list.add("AIR AIR AIR");

		String old = list.get(n / 3).split(" ")[n % 3];
		List<String> line = Arrays.asList(list.get(n / 3).split("\\ "));
		while (line.size() < 3)
			line.add("AIR");
		line.set(n % 3, material.name() + (durability > 0 ? ":" + durability : ""));
		String line_format = line.toString();
		line_format = line_format.replace("[", "").replace("]", "").replace(",", "");
		list.set(n / 3, line_format);

		config.getConfig().set(inv.getItemId() + ".craft", list);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + old + " changed to " + material.name() + (durability > 0 ? ":" + durability : "") + ".");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("craft"))
			lore.add(ChatColor.RED + "No crafting recipe.");
		else if (config.getStringList(path + ".craft").isEmpty())
			lore.add(ChatColor.RED + "No crafting recipe.");
		else
			for (String s1 : config.getStringList(path + ".craft")) {
				lore.add(ChatColor.GRAY + s1);
			}
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to edit the crafting recipe.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the crafting recipe.");
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
