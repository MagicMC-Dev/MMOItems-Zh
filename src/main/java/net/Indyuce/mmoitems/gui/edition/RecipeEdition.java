package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.recipe.MMORecipeChoice;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class RecipeEdition extends EditionInventory {
	private final boolean shapeless;

	public RecipeEdition(Player player, Type type, String id, boolean shapeless) {
		super(player, type, id);
		this.shapeless = shapeless;
	}

	@Override
	public Inventory getInventory() {
		return shapeless ? setupShapelessInventory() : setupShapedInventory();
	}

	private Inventory setupShapedInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Recipe Editor: " + id);

		ConfigFile config = type.getConfigFile();
		if (!config.getConfig().contains(id + ".crafting.shaped.1")) {
			config.getConfig().set(id + ".crafting.shaped.1", new String[] { "AIR AIR AIR", "AIR AIR AIR", "AIR AIR AIR" });
			registerItemEdition(config);
		}
		List<String> recipe = config.getConfig().getStringList(id + ".crafting.shaped.1");
		if (recipe.size() < 3) {
			while (recipe.size() < 3)
				recipe.add("AIR AIR AIR");

			config.getConfig().set(id + ".crafting.shaped.1", recipe);
			registerItemEdition(config);
		}
		for (int j = 0; j < 9; j++) {
			int slot = intToSlot(j);
			List<String> line = Arrays.asList(recipe.get(j / 3).split("\\ "));
			while (line.size() < 3)
				line.add("AIR");

			ItemStack element = new MMORecipeChoice(fixAir(line.get(j % 3))).generateStack();
			ItemMeta elementMeta = element.getItemMeta();
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
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Recipe Editor: " + id);
		ConfigFile config = type.getConfigFile();
		if (!config.getConfig().contains(id + ".crafting.shapeless.1")) {
			for (int i = 1; i < 10; i++)
				config.getConfig().set(id + ".crafting.shapeless.1.item" + i, "AIR");
			registerItemEdition(config);
		}
		for (int j = 0; j < 9; j++) {
			int slot = intToSlot(j);

			ItemStack element = new MMORecipeChoice(fixAir(config.getConfig().getString(id + ".crafting.shapeless.1.item" + (j + 1)))).generateStack();
			ItemMeta elementMeta = element.getItemMeta();
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

	private String fixAir(String string) {
		return string.equals("AIR") ? "BARRIER" : string;
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
				new StatEdition(this, ItemStat.CRAFTING, "recipe", (shapeless ? "shapeless" : "shaped"), slotToInt(event.getRawSlot())).enable("Write in the chat the item you want.", "Format: '[MATERIAL]' or '[MATERIAL]:[DURABILITY]' or '[TYPE].[ID]'");

		} else if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (shapeless)
				deleteShapeless(slotToInt(event.getRawSlot()));
			else
				deleteShaped(slotToInt(event.getRawSlot()));
		}
	}

	private void deleteShaped(int slot) {
		ConfigFile config = type.getConfigFile();
		List<String> newList = config.getConfig().getStringList(id + ".crafting.shaped.1");
		String[] newArray = newList.get((int) Math.floor(slot / 3)).split("\\ ");
		newArray[slot % 3] = "AIR";
		newList.set((int) Math.floor(slot / 3), (newArray[0] + " " + newArray[1] + " " + newArray[2]));

		config.getConfig().set(id + ".crafting.shaped.1", newList);
		registerItemEdition(config);
		open();
	}

	private void deleteShapeless(int slot) {
		ConfigFile config = type.getConfigFile();
		config.getConfig().set(id + ".crafting.shapeless.1.item" + (slot + 1), "AIR");
		registerItemEdition(config);
		open();
	}
}