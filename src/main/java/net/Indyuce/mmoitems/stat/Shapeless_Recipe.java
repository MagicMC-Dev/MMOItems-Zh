package net.Indyuce.mmoitems.stat;

import java.util.ArrayList;
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
import net.mmogroup.mmolib.version.VersionMaterial;

public class Shapeless_Recipe extends StringStat {
	public Shapeless_Recipe() {
		super(new ItemStack(VersionMaterial.OAK_PLANKS.toMaterial()), "Shapeless Recipe", new String[] { "A shapeless recipe has no ingredient", "pattern in the workbench.", "Changing this value requires a reload.", "", "&9In order to disable the shapeless recipe,", "&9just remove all the ingredients from it." }, "shapeless-craft", new String[] { "all" });
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.SHAPELESS_RECIPE).enable("Write in the chat the ingredient you want to add.", ChatColor.AQUA + "Format: [MATERIAL]");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("shapeless-craft")) {
				List<String> shapelessRecipe = config.getConfig().getStringList(inv.getItemId() + ".shapeless-craft");
				if (shapelessRecipe.size() < 1)
					return true;

				String last = shapelessRecipe.get(shapelessRecipe.size() - 1);
				shapelessRecipe.remove(last);
				config.getConfig().set(inv.getItemId() + ".shapeless-craft", shapelessRecipe.isEmpty() ? null : shapelessRecipe);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed '" + last + "'.");
			}
		}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		Material material = null;
		String format = message.toUpperCase().replace("-", "_").replace(" ", "_");
		try {
			material = Material.valueOf(format);
		} catch (Exception e1) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + format + " is not a valid material!");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "All materials can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");
			return false;
		}

		List<String> list = config.getConfig().getConfigurationSection(inv.getItemId()).contains("shapeless-craft") ? config.getConfig().getStringList(inv.getItemId() + ".shapeless-craft") : new ArrayList<>();
		list.add(material.name());

		config.getConfig().set(inv.getItemId() + ".shapeless-craft", list);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + MMOUtils.caseOnWords(material.name().replace("_", " ").toLowerCase()) + " successfully added to the shapeless crafting recipe.");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.GRAY + "Current Value:");
		if (!config.getConfigurationSection(path).contains("shapeless-craft"))
			lore.add(ChatColor.RED + "No shapeless recipe.");
		else if (config.getStringList(path + ".shapeless-craft").isEmpty())
			lore.add(ChatColor.RED + "No shapeless recipe.");
		else
			for (String s1 : config.getStringList(path + ".shapeless-craft"))
				lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + s1);
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add an ingredient.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last ingredient.");
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
