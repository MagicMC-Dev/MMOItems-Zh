package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Element;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class ElementsEdition extends EditionInventory {
	public static Map<Integer, String> correspondingSlot = new HashMap<>();

	public ElementsEdition(Player player, Type type, String id) {
		super(player, type, id);
		
		if (correspondingSlot.isEmpty()) {
			correspondingSlot.put(19, "fire.damage");
			correspondingSlot.put(25, "fire.defense");
			correspondingSlot.put(20, "ice.damage");
			correspondingSlot.put(24, "ice.defense");
			correspondingSlot.put(28, "wind.damage");
			correspondingSlot.put(34, "wind.defense");
			correspondingSlot.put(29, "earth.damage");
			correspondingSlot.put(33, "earth.defense");
			correspondingSlot.put(30, "thunder.damage");
			correspondingSlot.put(32, "thunder.defense");
			correspondingSlot.put(37, "water.damage");
			correspondingSlot.put(43, "water.defense");
		}
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Elements E.: " + id);
		int[] slots = { 19, 25, 20, 24, 28, 34, 29, 33, 30, 32, 37, 43, 38, 42, 39, 41 };
		int n = 0;

		FileConfiguration config = type.getConfigFile().getConfig();
		for (Element element : Element.values()) {
			ItemStack attack = element.getItem().clone();
			ItemMeta attackMeta = attack.getItemMeta();
			attackMeta.setDisplayName(ChatColor.GREEN + element.getName() + " Damage");
			List<String> attackLore = new ArrayList<String>();
			attackLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN + config.getDouble(id + ".element." + element.getName().toLowerCase() + ".damage"));
			attackLore.add("");
			attackLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
			attackLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
			attackMeta.setLore(attackLore);
			attack.setItemMeta(attackMeta);

			ItemStack defense = element.getItem().clone();
			ItemMeta defenseMeta = defense.getItemMeta();
			defenseMeta.setDisplayName(ChatColor.GREEN + element.getName() + " Defense");
			List<String> defenseLore = new ArrayList<String>();
			defenseLore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN + config.getDouble(id + ".element." + element.getName().toLowerCase() + ".defense"));
			defenseLore.add("");
			defenseLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this value.");
			defenseLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
			defenseMeta.setLore(defenseLore);
			defense.setItemMeta(defenseMeta);

			inv.setItem(slots[n], attack);
			inv.setItem(slots[n + 1], defense);
			n += 2;
		}

		addEditionInventoryItems(inv, true);

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isPluginItem(item, false))
			return;

		if (correspondingSlot.containsKey(event.getSlot())) {
			if (event.getAction() == InventoryAction.PICKUP_ALL)
				new StatEdition(this, ItemStat.ELEMENTS, event.getSlot()).enable("Write in the value you want.");

			if (event.getAction() == InventoryAction.PICKUP_HALF) {
				ConfigFile config = type.getConfigFile();
				String elementPath = correspondingSlot.get(event.getSlot());
				config.getConfig().set(id + ".element." + elementPath, null);

				// clear element config section
				String elementName = elementPath.split("\\.")[0];
				if (config.getConfig().getConfigurationSection(id).contains("element")) {
					if (config.getConfig().getConfigurationSection(id + ".element").contains(elementName))
						if (config.getConfig().getConfigurationSection(id + ".element." + elementName).getKeys(false).isEmpty())
							config.getConfig().set(id + ".element." + elementName, null);
					if (config.getConfig().getConfigurationSection(id + ".element").getKeys(false).isEmpty())
						config.getConfig().set(id + ".element", null);
				}

				registerItemEdition(config);
				new ElementsEdition(player, type, id).open(getPreviousPage());
				player.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + MMOUtils.caseOnWords(elementPath.replace(".", " ")) + ChatColor.GRAY + " successfully removed.");
			}
		}
	}
}