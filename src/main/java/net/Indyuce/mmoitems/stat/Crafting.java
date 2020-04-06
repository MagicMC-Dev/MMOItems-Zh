package net.Indyuce.mmoitems.stat;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.CraftingEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class Crafting extends ItemStat {
	public Crafting() {
		super("CRAFTING", new ItemStack(VersionMaterial.CRAFTING_TABLE.toMaterial()), "Crafting", new String[] { "The crafting recipes of your item.", "Changing a recipe requires &o/mi reload recipes&7." }, new String[] { "all" });
	}

	@Override
	public boolean whenClicked(EditionInventory inv, InventoryClickEvent event) {
		if (event.getAction() == InventoryAction.PICKUP_ALL)
			new CraftingEdition(inv.getPlayer(), inv.getItemType(), inv.getItemId()).open(inv.getPage());
		else if (event.getAction() == InventoryAction.PICKUP_HALF) {
			ConfigFile config = inv.getItemType().getConfigFile();
			ConfigurationSection section = config.getConfig().getConfigurationSection(inv.getItemId());
			if (section.contains("crafting")) {
				section.set("crafting", null);
				inv.registerItemEdition(config);
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Crafting recipes successfully removed. Make sure you reload active recipes using " + ChatColor.RED + "/mi reload recipes" + ChatColor.GRAY + ".");
			}
		}
		return true;
	}

	@Override
	public void whenDisplayed(List<String> lore, FileConfiguration config, String path) {
		lore.add("");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to access the crafting edition menu.");
		lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove all crafting recipes.");
	}

	@Override
	public boolean whenInput(EditionInventory inv, ConfigFile config, String message, Object... info) {
		String type = (String) info[0];

		if (type.equals("recipe")) {
			int slot = (int) info[2];

			if (validate(inv.getPlayer(), message)) {
				if (((String) info[1]).equals("shaped")) {
					List<String> newList = config.getConfig().getStringList(inv.getItemId() + ".crafting.shaped.1");
					String[] newArray = newList.get((int) Math.floor(slot / 3)).split("\\ ");
					newArray[slot % 3] = message;
					newList.set((int) Math.floor(slot / 3), (newArray[0] + " " + newArray[1] + " " + newArray[2]));

					config.getConfig().set(inv.getItemId() + ".crafting.shaped.1", newList);
					inv.registerItemEdition(config);
				} else {
					config.getConfig().set(inv.getItemId() + ".crafting.shapeless.1.item" + (slot + 1), message);
					inv.registerItemEdition(config);
				}
			}
		} else if (type.equals("item")) {
			String[] args = message.split("\\ ");
			if (args.length != 3) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Invalid format.");
				return false;
			}

			if (!validate(inv.getPlayer(), args[0]))
				return false;
			int time;
			try {
				time = Integer.parseInt(args[1]);
			} catch (Exception e1) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + args[1] + " is not a valid number.");
				return false;
			}
			double exp;
			try {
				exp = Double.parseDouble(args[2]);
			} catch (Exception e1) {
				inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + args[2] + " is not a valid number.");
				return false;
			}

			config.getConfig().set(inv.getItemId() + ".crafting." + info[1] + ".1.item", args[0]);
			config.getConfig().set(inv.getItemId() + ".crafting." + info[1] + ".1.time", time);
			config.getConfig().set(inv.getItemId() + ".crafting." + info[1] + ".1.experience", exp);
			inv.registerItemEdition(config);
		} else
			MMOItems.plugin.getLogger().warning("Something went wrong!");

		return true;
	}

	@Override
	public void whenLoaded(MMOItem item, ConfigurationSection config) {
	}

	@Override
	public boolean whenApplied(MMOItemBuilder item, StatData data) {
		return true;
	}

	@Override
	public void whenLoaded(MMOItem mmoitem, NBTItem item) {
	}

	private boolean validate(Player player, String input) {
		if (input.contains(".")) {
			String[] typeid = input.split("\\.");
			if (typeid.length != 2) {
				player.sendMessage(MMOItems.plugin.getPrefix() + "Invalid format.");
				return false;
			}
			if (!Type.isValid(typeid[0].toUpperCase().replace("-", "_").replace(" ", "_"))) {
				player.sendMessage(MMOItems.plugin.getPrefix() + "'" + typeid[0].toUpperCase().replace("-", "_").replace(" ", "_") + "' isn't a valid item type.");
				return false;
			}

			Type type = Type.get(typeid[0].toUpperCase().replace("-", "_").replace(" ", "_"));
			if (MMOItems.plugin.getItems().getItem(type, typeid[1]) == null) {
				player.sendMessage(MMOItems.plugin.getPrefix() + "Could not find item with ID '" + typeid[1].toUpperCase().replace("-", "_").replace(" ", "_") + "'.");
				return false;
			}

			return true;
		}
		if (input.contains(":")) {
			String[] matmeta = input.split("\\:");
			if (matmeta.length != 2) {
				player.sendMessage(MMOItems.plugin.getPrefix() + "Invalid format.");
				return false;
			}
			try {
				Material.valueOf(matmeta[0].toUpperCase().replace("-", "_"));
			} catch (IllegalArgumentException exception) {
				player.sendMessage(MMOItems.plugin.getPrefix() + "'" + matmeta[0].toUpperCase().replace("-", "_") + "' isn't a valid material.");
				return false;
			}
			try {
				Integer.parseInt(matmeta[1]);
			} catch (NumberFormatException exception) {
				player.sendMessage(MMOItems.plugin.getPrefix() + "'" + matmeta[1] + "' isn't a valid number.");
				return false;
			}

			return true;
		}
		try {
			Material.valueOf(input.toUpperCase().replace("-", "_"));
		} catch (Exception e) {
			player.sendMessage(MMOItems.plugin.getPrefix() + "'" + input.toUpperCase().replace("-", "_") + "' isn't a valid material.");
			return false;
		}

		return true;
	}
}
