package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.version.VersionMaterial;

public class AdvancedRecipeEdition extends EditionInventory implements Listener {

	// closing an inventory while there's an item on the cursor
	// makes the item drop ; add UUID to this set so the item doesn't drop
	public static Set<UUID> noDrop = new HashSet<>();

	public AdvancedRecipeEdition(Player player, Type type, String id) {
		super(player, type, id);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "CRecipe: " + id);

		// initialize config for recipe
		ConfigFile config = type.getConfigFile();
		if (!config.getConfig().getConfigurationSection(id).contains("advanced-craft")) {
			for (int j = 0; j < 9; j++) {
				config.getConfig().set(id + ".advanced-craft." + j + ".material", "AIR");
				config.getConfig().set(id + ".advanced-craft." + j + ".amount", 1);
				config.getConfig().set(id + ".advanced-craft." + j + ".name", "");
			}
			registerItemEdition(config);
		}

		// display ingredient
		for (int j = 0; j < 9; j++) {
			Material material = null;
			try {
				material = Material.valueOf(config.getConfig().getString(id + ".advanced-craft." + j + ".material").toUpperCase().replace("-", "_").replace(" ", "_"));
			} catch (Exception event) {
				material = VersionMaterial.NETHER_WART.toMaterial();
			}

			// type/id overrides material/name/damage
			String id1 = config.getConfig().getString(id + ".advanced-craft." + j + ".id");
			String type1 = config.getConfig().getString(id + ".advanced-craft." + j + ".type");
			boolean mmoitemOverride = id1 != null && !id1.equals("") && type1 != null && !type1.equals("");

			ItemStack ingredient = mmoitemOverride ? MMOItems.plugin.getItems().getItem(MMOItems.plugin.getTypes().get(type1), id1) : new ItemStack(material);
			if (ingredient.getType() == Material.AIR)
				ingredient.setType(Material.BARRIER);
			ingredient.setAmount(config.getConfig().getInt(id + ".advanced-craft." + j + ".amount"));
			ItemMeta ingredientMeta = ingredient.getItemMeta();
			List<String> ingredientLore = new ArrayList<String>();
			ingredientLore.add(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------");
			if (!mmoitemOverride) {
				String name = config.getConfig().getString(id + ".advanced-craft." + j + ".name");
				ingredientMeta.setDisplayName(name);
				ingredientLore.add(ChatColor.WHITE + "Material = " + ingredient.getType().name());
				ingredientLore.add(ChatColor.WHITE + "Amount = " + ingredient.getAmount());
				ingredientLore.add(ChatColor.WHITE + "Name = " + (name.equals("") ? ChatColor.RED + "No Name" : ingredientMeta.getDisplayName()));
			} else {
				ingredientLore.add(ChatColor.WHITE + "Type = " + type1);
				ingredientLore.add(ChatColor.WHITE + "Item ID = " + id1);
				ingredientLore.add(ChatColor.WHITE + "Amount = " + ingredient.getAmount());
			}
			ingredientLore.add(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "----------------------");
			ingredientLore.add("");
			ingredientLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "Drag & drop an item here");
			ingredientLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "to change this ingredient.");
			ingredientLore.add("");
			ingredientLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove.");
			ingredientMeta.setLore(ingredientLore);
			ingredient.setItemMeta(ingredientMeta);

			inv.setItem(intToSlot(j), ingredient);
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
		if (event.getInventory() == event.getClickedInventory())
			event.setCancelled(true);

		int slotInt = slotToInt(event.getSlot());
		if (slotInt < 0 || event.getInventory() != event.getClickedInventory())
			return;

		ConfigFile config = type.getConfigFile();

		// reset item if right click
		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			config.getConfig().set(id + ".advanced-craft." + slotInt + ".id", null);
			config.getConfig().set(id + ".advanced-craft." + slotInt + ".type", null);

			config.getConfig().set(id + ".advanced-craft." + slotInt + ".material", "AIR");
			config.getConfig().set(id + ".advanced-craft." + slotInt + ".amount", 1);
			config.getConfig().set(id + ".advanced-craft." + slotInt + ".name", "");
			registerItemEdition(config);
			open();
			return;
		}

		ItemStack cursor = event.getCursor();
		if (cursor == null || cursor.getType() == Material.AIR)
			return;

		// type/id overrides material/name/damage
		NBTItem nbtCursor = MMOItems.plugin.getNMS().getNBTItem(cursor);
		String id1 = nbtCursor.getString("MMOITEMS_ITEM_ID");
		String type1 = nbtCursor.getString("MMOITEMS_ITEM_TYPE");
		if (id1 == null || id1.equals("") || type1 == null || type1.equals("")) {
			config.getConfig().set(id + ".advanced-craft." + slotInt, null);
			config.getConfig().set(id + ".advanced-craft." + slotInt + ".material", cursor.getType().name());
			config.getConfig().set(id + ".advanced-craft." + slotInt + ".amount", cursor.getAmount());
			config.getConfig().set(id + ".advanced-craft." + slotInt + ".name", cursor.getItemMeta().hasDisplayName() ? cursor.getItemMeta().getDisplayName() : "");
		} else {
			config.getConfig().set(id + ".advanced-craft." + slotInt, null);
			config.getConfig().set(id + ".advanced-craft." + slotInt + ".type", type1);
			config.getConfig().set(id + ".advanced-craft." + slotInt + ".id", id1);
			config.getConfig().set(id + ".advanced-craft." + slotInt + ".amount", cursor.getAmount());
		}

		registerItemEdition(config);

		noDrop.add(player.getUniqueId());
		open();
	}
}