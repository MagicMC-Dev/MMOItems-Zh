package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.AdvancedRecipeEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import net.Indyuce.mmoitems.version.VersionMaterial;

public class Advanced_Crafting_Recipe extends StringStat {
	public Advanced_Crafting_Recipe() {
		super(new ItemStack(VersionMaterial.CRAFTING_TABLE.toMaterial()), "Advanced Crafting Recipe", new String[] { "The advanced recipe of your item.", "Changing this value requires &o/mi reload adv-recipes&7." }, "advanced-craft", new String[] { "all" });
	}

	@Override
	public boolean whenLoaded(MMOItem item, ConfigurationSection config) {
		return true;
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new AdvancedRecipeEdition(inv.getPlayer(), inv.getItemType(), inv.getItemId()).open();
		if (event.getAction() == InventoryAction.PICKUP_HALF)
			if (config.getConfig().getConfigurationSection(inv.getItemId()).contains("advanced-craft")) {
				config.getConfig().set(inv.getItemId() + ".advanced-craft", null);
				inv.registerItemEdition(config);
				inv.open();
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Advanced Crafting Recipe successfully removed.");
			}
		return true;
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to edit the advanced crafting recipe.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the advanced crafting recipe.");
	}
}
