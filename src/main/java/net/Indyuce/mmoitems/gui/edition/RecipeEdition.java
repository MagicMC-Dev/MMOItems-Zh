package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.api.util.AltChar;

public class RecipeEdition extends EditionInventory {
	private final boolean shapeless;

	public RecipeEdition(Player player, MMOItem mmoitem, boolean shapeless) {
		super(player, mmoitem);

		this.shapeless = shapeless;
	}

	@Override
	public Inventory getInventory() {
		return shapeless ? setupShapelessInventory() : setupShapedInventory();
	}

	private Inventory setupShapedInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Recipe Editor: " + mmoitem.getId());

		ConfigFile config = mmoitem.getType().getConfigFile();
		if (!config.getConfig().contains(mmoitem.getId() + ".crafting.shaped.1")) {
			config.getConfig().set(mmoitem.getId() + ".crafting.shaped.1", new String[] { "AIR AIR AIR", "AIR AIR AIR", "AIR AIR AIR" });
			registerItemEdition(config);
		}
		List<String> recipe = config.getConfig().getStringList(mmoitem.getId() + ".crafting.shaped.1");
		if (recipe.size() < 3) {
			while (recipe.size() < 3)
				recipe.add("AIR AIR AIR");

			config.getConfig().set(mmoitem.getId() + ".crafting.shaped.1", recipe);
			registerItemEdition(config);
		}
		for (int j = 0; j < 9; j++) {
			int slot = intToSlot(j);
			List<String> line = Arrays.asList(recipe.get(j / 3).split("\\ "));
			while (line.size() < 3)
				line.add("AIR");

			ItemStack element = MMOItems.plugin.parseStack(line.get(j % 3));
			if(element == null) element = new ItemStack(Material.BARRIER);
			if(element.getType() == Material.AIR) element.setType(Material.BARRIER);
			ItemMeta elementMeta = element.getItemMeta();
			if(element.getType() == Material.BARRIER) elementMeta.setDisplayName(ChatColor.RED + "Empty");
			List<String> elementLore = new ArrayList<>();
			elementLore.add("");
			elementLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this ingredient.");
			elementLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this ingredient.");
			elementMeta.setLore(elementLore);
			element.setItemMeta(elementMeta);

			inv.setItem(slot, element);
		}

		addEditionInventoryItems(inv, true);
		return inv;
	}

	private Inventory setupShapelessInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Recipe Editor: " + mmoitem.getId());
		ConfigFile config = mmoitem.getType().getConfigFile();
		if (!config.getConfig().contains(mmoitem.getId() + ".crafting.shapeless.1")) {
			config.getConfig().set(mmoitem.getId() + ".crafting.shapeless.1",
				Arrays.asList("AIR", "AIR", "AIR", "AIR", "AIR", "AIR", "AIR", "AIR", "AIR"));
			registerItemEdition(config);
		}
		List<String> ingredients = config.getConfig().getStringList(mmoitem.getId() + ".crafting.shapeless.1");
		if(ingredients.size() == 9)
			for (int j = 0; j < 9; j++) {
				int slot = intToSlot(j);

				ItemStack element = MMOItems.plugin.parseStack(ingredients.get(j));
				if(element == null) element = new ItemStack(Material.BARRIER);
				if(element.getType() == Material.AIR) element.setType(Material.BARRIER);
				ItemMeta elementMeta = element.getItemMeta();
				if(element.getType() == Material.BARRIER) elementMeta.setDisplayName(ChatColor.RED + "Empty");
				List<String> elementLore = new ArrayList<>();
				elementLore.add("");
				elementLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this ingredient.");
				elementLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this ingredient.");
				elementMeta.setLore(elementLore);
				element.setItemMeta(elementMeta);

				inv.setItem(slot, element);
			}
		else MMOItems.plugin.getLogger().warning("Couldn't load shapeless recipe for '" + mmoitem.getId() + "'!");

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

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory())
			return;

		if (event.getAction() == InventoryAction.PICKUP_ALL) {
			if (slotToInt(event.getRawSlot()) >= 0)
				new StatEdition(this, ItemStat.CRAFTING, "recipe", (shapeless ? "shapeless" : "shaped"), slotToInt(event.getRawSlot()))
						.enable("Write in the chat the item you want.", "Format: '[MATERIAL]' or '[TYPE].[ID]'");

		} else if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (shapeless)
				deleteShapeless(slotToInt(event.getRawSlot()));
			else
				deleteShaped(slotToInt(event.getRawSlot()));
		}
	}

	private void deleteShaped(int slot) {
		ConfigFile config = mmoitem.getType().getConfigFile();
		List<String> newList = config.getConfig().getStringList(mmoitem.getId() + ".crafting.shaped.1");
		String[] newArray = newList.get((int) Math.floor(slot / 3)).split("\\ ");
		newArray[slot % 3] = "AIR";
		newList.set((int) Math.floor(slot / 3), (newArray[0] + " " + newArray[1] + " " + newArray[2]));

		config.getConfig().set(mmoitem.getId() + ".crafting.shaped.1", newList);
		registerItemEdition(config);
		open();
	}

	private void deleteShapeless(int slot) {
		ConfigFile config = mmoitem.getType().getConfigFile();
		if(config.getConfig().contains(mmoitem.getId() + ".crafting.shapeless.1")) {
			List<String> newList = config.getConfig().getStringList(mmoitem.getId() + ".crafting.shapeless.1");
			newList.set(slot, "AIR");
			config.getConfig().set(mmoitem.getId() + ".crafting.shapeless.1", newList);
			registerItemEdition(config);
			open();
		}
	}
}