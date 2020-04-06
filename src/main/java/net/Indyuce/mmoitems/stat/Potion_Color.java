package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.ColorData;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StringStat;

public class Potion_Color extends StringStat {
	public Potion_Color() {
		super("POTION_COLOR", new ItemStack(Material.POTION), "Potion Color", new String[] { "The color of your potion.", "(Doesn't impact the effects)." }, new String[] { "all" }, Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW);
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		ConfigFile config = inv.getItemType().getConfigFile();
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new StatEdition(inv, ItemStat.POTION_COLOR).enable("Write in the chat the RGB color you want.", ChatColor.AQUA + "Format: [RED] [GREEN] [BLUE]");

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			config.getConfig().set(inv.getItemId() + ".potion-color", null);
			inv.registerItemEdition(config);
			inv.open();
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed Potion Color.");
		}
		return true;
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String[] split = message.split("\\ ");
		if (split.length != 3) {
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "" + message + " is not a valid [RED] [GREEN] [BLUE].");
			inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Example: '75 0 130' stands for Indigo Purple.");
			return false;
		}
		for (String str : split)
			try {
				int k = Integer.parseInt(str);
				Validate.isTrue(k >= 0 && k < 256, "Color must be between 0 and 255");
			} catch (IllegalArgumentException exception) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + str + " is not a valid number (must be between 0 and 255).");
				return false;
			}

		config.getConfig().set(inv.getItemId() + ".potion-color", message);
		inv.registerItemEdition(config);
		inv.open();
		inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Potion Color successfully changed to " + message + ".");
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		if (!config.getConfigurationSection(path).contains("potion-color")) {
			lore.add(ChatColor.GRAY + "Current Value:");
			lore.add(ChatColor.RED + "No value.");
		} else
			lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN + config.getString(path + ".potion-color"));
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the potion color.");
	}

	@Override
	public void whenLoaded(MMOItem item, ConfigurationSection config) {
		item.setData(ItemStat.POTION_COLOR, new ColorData(item, config.getString("potion-color")));
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		if (item.getItemStack().getType().name().contains("POTION") || item.getItemStack().getType() == Material.TIPPED_ARROW)
			((PotionMeta) item.getMeta()).setColor(((ColorData) data).getColor());
		return true;
	}
}
