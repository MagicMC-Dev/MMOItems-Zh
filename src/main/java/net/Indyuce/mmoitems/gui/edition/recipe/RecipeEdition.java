package net.Indyuce.mmoitems.gui.edition.recipe;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.WorkbenchIngredient;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import io.lumine.mythic.lib.api.util.AltChar;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecipeEdition extends EditionInventory {
	private final boolean shapeless;

	public RecipeEdition(Player player, MMOItemTemplate template, boolean shapeless) {
		super(player, template);

		this.shapeless = shapeless;
	}

	@Override
	public Inventory getInventory() {
		return shapeless ? setupShapelessInventory() : setupShapedInventory();
	}

	private Inventory setupShapedInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, "Recipe Editor: " + template.getId());

		if (!getEditedSection().contains("crafting.shaped.1")) {
			getEditedSection().set("crafting.shaped.1", new String[] { "AIR AIR AIR", "AIR AIR AIR", "AIR AIR AIR" });
			registerTemplateEdition();
		}
		List<String> recipe = getEditedSection().getStringList("crafting.shaped.1");
		if (recipe.size() < 3) {
			while (recipe.size() < 3)
				recipe.add("AIR AIR AIR");

			getEditedSection().set("crafting.shaped.1", recipe);
			registerTemplateEdition();
		}
		for (int j = 0; j < 9; j++) {
			int slot = intToSlot(j);
			List<String> line = Arrays.asList(recipe.get(j / 3).split(" "));
			while (line.size() < 3)
				line.add("AIR");

			ItemStack element;
			try {
				WorkbenchIngredient ingredient = MMOItems.plugin.getRecipes().getWorkbenchIngredient(line.get(j % 3));
				element = ingredient.generateItem();
				element.setAmount(ingredient.getAmount());
				Validate.isTrue(element != null && element.getType() != Material.AIR);
			} catch (IllegalArgumentException exception) {
				element = new ItemStack(Material.BARRIER);
			}
			ItemMeta elementMeta = element.getItemMeta();
			if (element.getType() == Material.BARRIER)
				elementMeta.setDisplayName(ChatColor.RED + "Empty");
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
		Inventory inv = Bukkit.createInventory(this, 54, "Recipe Editor: " + template.getId());
		if (!getEditedSection().contains("crafting.shapeless.1")) {
			getEditedSection().set("crafting.shapeless.1", Arrays.asList("AIR", "AIR", "AIR", "AIR", "AIR", "AIR", "AIR", "AIR", "AIR"));
			registerTemplateEdition();
		}
		List<String> ingredients = getEditedSection().getStringList("crafting.shapeless.1");
		if (ingredients.size() == 9)
			for (int j = 0; j < 9; j++) {
				int slot = intToSlot(j);

				ItemStack element;
				try {
					WorkbenchIngredient ingredient = MMOItems.plugin.getRecipes().getWorkbenchIngredient(ingredients.get(j));
					element = ingredient.generateItem();
					element.setAmount(ingredient.getAmount());
					Validate.isTrue(element != null && element.getType() != Material.AIR);
				} catch (IllegalArgumentException exception) {
					element = new ItemStack(Material.BARRIER);
				}
				ItemMeta elementMeta = element.getItemMeta();
				if (element.getType() == Material.BARRIER)
					elementMeta.setDisplayName(ChatColor.RED + "Empty");
				List<String> elementLore = new ArrayList<>();
				elementLore.add("");
				elementLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to change this ingredient.");
				elementLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this ingredient.");
				elementMeta.setLore(elementLore);
				element.setItemMeta(elementMeta);

				inv.setItem(slot, element);
			}
		else
			MMOItems.plugin.getLogger().warning("Couldn't load shapeless recipe for '" + template.getId() + "'!");

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
				new StatEdition(this, ItemStats.CRAFTING, "recipe", (shapeless ? "shapeless" : "shaped"), slotToInt(event.getRawSlot()))
						.enable("Write in the chat the item you want.", "Format: '[MATERIAL]' or '[TYPE].[ID]'");

		} else if (event.getAction() == InventoryAction.PICKUP_HALF) {
			if (shapeless)
				deleteShapeless(slotToInt(event.getRawSlot()));
			else
				deleteShaped(slotToInt(event.getRawSlot()));
		}
	}

	private void deleteShaped(int slot) {
		List<String> newList = getEditedSection().getStringList("crafting.shaped.1");
		String[] newArray = newList.get(slot / 3).split(" ");
		newArray[slot % 3] = "AIR";
		newList.set(slot / 3, (newArray[0] + " " + newArray[1] + " " + newArray[2]));

		getEditedSection().set("crafting.shaped.1", newList);
		registerTemplateEdition();
	}

	private void deleteShapeless(int slot) {
		if (getEditedSection().contains("crafting.shapeless.1")) {
			List<String> newList = getEditedSection().getStringList("crafting.shapeless.1");
			newList.set(slot, "AIR");
			getEditedSection().set("crafting.shapeless.1", newList);
			registerTemplateEdition();
		}
	}
}