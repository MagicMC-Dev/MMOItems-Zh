package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.version.VersionMaterial;

public class RecipeEdition extends EditionInventory {
	public RecipeEdition(Player player, Type type, String id) {
		super(player, type, id);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Recipe E.: " + id);

		ConfigFile config = type.getConfigFile();
		if (!config.getConfig().getConfigurationSection(id).contains("craft")) {
			config.getConfig().set(id + ".craft", new String[] { "AIR AIR AIR", "AIR AIR AIR", "AIR AIR AIR" });
			registerItemEdition(config);
		}
		List<String> recipe = config.getConfig().getStringList(id + ".craft");
		if (recipe.size() < 3) {
			while (recipe.size() < 3)
				recipe.add("AIR AIR AIR");

			config.getConfig().set(id + ".craft", recipe);
			registerItemEdition(config);
		}
		for (int j = 0; j < 9; j++) {
			int slot = intToSlot(j);
			List<String> line = Arrays.asList(recipe.get(j / 3).split("\\ "));
			while (line.size() < 3)
				line.add("AIR");

			String material = line.get(j % 3).replace("-", "_");
			String[] split = material.split("\\:");

			Material m = null;
			try {
				m = Material.getMaterial(split[0]);
			} catch (Exception e) {
			}

			String name = "";
			if (m == null) {
				m = VersionMaterial.GRAY_STAINED_GLASS_PANE.toMaterial();
				name = ChatColor.RED + "[INVALID] " + material;
			} else if (m.equals(Material.AIR)) {
				name = ChatColor.GREEN + "AIR";
				m = Material.BARRIER;
			} else
				name = ChatColor.GREEN + m.name();

			ItemStack element = new ItemStack(m);
			ItemMeta elementMeta = element.getItemMeta();
			List<String> elementLore = new ArrayList<String>();
			elementLore.add("");
			elementLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this ingredient.");
			elementMeta.setLore(elementLore);
			elementMeta.setDisplayName(name);
			element.setItemMeta(elementMeta);

			inv.setItem(slot, element);
		}

		addEditionInventoryItems(inv, true);

		return inv;
	}

	private int intToSlot(int i) {
		return i >= 0 && i <= 2 ? 21 + i : (i >= 3 && i <= 5 ? 27 + i : (i >= 6 && i <= 8 ? 33 + i : 0));
	}

	private int slotToInt(int i) {
		return i >= 21 && i <= 23 ? i - 21 : (i >= 30 && i <= 32 ? i - 27 : (i >= 39 && i <= 41 ? i - 33 : -1));
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (!MMOUtils.isPluginItem(item, false) || event.getInventory() != event.getClickedInventory())
			return;

		if (slotToInt(event.getSlot()) > -1)
			new StatEdition(this, ItemStat.CRAFTING_RECIPE, slotToInt(event.getSlot())).enable("Write in the chat the material you want.", "Format: [MATERIAL] or [MATERIAL]:[DURABILITY]");
	}
}