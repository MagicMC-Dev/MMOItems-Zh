package net.Indyuce.mmoitems.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.edition.NewItemEdition;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import net.Indyuce.mmoitems.version.VersionMaterial;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class ItemBrowser extends PluginInventory {
	private Type type;
	private boolean deleteMode;
	private Map<String, ItemStack> cached = new HashMap<>();

	private static final int[] slots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34 };

	public ItemBrowser(Player player) {
		this(player, null);
	}

	public ItemBrowser(Player player, Type type) {
		super(player);
		this.type = type;
	}

	@Override
	public Inventory getInventory() {
		int min = (page - 1) * slots.length;
		int max = page * slots.length;
		int n = 0;

		/*
		 * displays all possible item types if no type was previously selected
		 * by the player
		 */
		if (type == null) {
			Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Item Explorer");
			List<Type> types = new ArrayList<>(MMOItems.plugin.getTypes().getAll());
			for (int j = min; j < Math.min(max, types.size()); j++) {
				Type type = types.get(j);
				int items = type.getConfigFile().getConfig().getKeys(false).size();

				ItemStack item = type.getItem();
				item.setAmount(Math.max(1, Math.min(64, items)));
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.GREEN + type.getName() + ChatColor.DARK_GRAY + " (Click to browse)");
				meta.addItemFlags(ItemFlag.values());
				List<String> lore = new ArrayList<>();
				lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "There " + (items != 1 ? "are" : "is") + " " + (items < 1 ? "" + ChatColor.RED + ChatColor.ITALIC + "no" : "" + ChatColor.GOLD + ChatColor.ITALIC + items) + ChatColor.GRAY + ChatColor.ITALIC + " item" + (items != 1 ? "s" : "") + " in that type.");
				meta.setLore(lore);
				item.setItemMeta(meta);

				inv.setItem(slots[n++], NBTItem.get(item).addTag(new ItemTag("typeId", type.getId())).toItem());
			}

			ItemStack glass = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
			ItemMeta glassMeta = glass.getItemMeta();
			glassMeta.setDisplayName(ChatColor.RED + "- No type -");
			glass.setItemMeta(glassMeta);

			ItemStack next = new ItemStack(Material.ARROW);
			ItemMeta nextMeta = next.getItemMeta();
			nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
			next.setItemMeta(nextMeta);

			ItemStack previous = new ItemStack(Material.ARROW);
			ItemMeta previousMeta = previous.getItemMeta();
			previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
			previous.setItemMeta(previousMeta);

			while (n < slots.length)
				inv.setItem(slots[n++], glass);
			inv.setItem(18, page > 1 ? previous : null);
			inv.setItem(26, inv.getItem(34).equals(glass) ? null : next);

			return inv;
		}


		ItemStack error = VersionMaterial.RED_STAINED_GLASS_PANE.toItem();
		ItemMeta errorMeta = error.getItemMeta();
		errorMeta.setDisplayName(ChatColor.RED + "- Error -");
		List<String> errorLore = new ArrayList<>();
		errorLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "An error occured while");
		errorLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "trying to generate that item.");
		errorMeta.setLore(errorLore);
		error.setItemMeta(errorMeta);

		List<String> itemIds = new ArrayList<>(type.getConfigFile().getConfig().getKeys(false));

		/*
		 * displays every item in a specific type. items are cached inside the
		 * map at the top to reduce performance impact and are directly rendered
		 */
		Inventory inv = Bukkit.createInventory(this, 54, (deleteMode ? (ChatColor.UNDERLINE + "DELETE MODE: ") : (ChatColor.UNDERLINE + "Item Explorer: ")) + type.getName());
		for (int j = min; j < Math.min(max, itemIds.size()); j++) {
			String id = itemIds.get(j);
			if (!cached.containsKey(id)) {
				ItemStack item = MMOItems.plugin.getItems().getItem(type, id);
				if (item == null || item.getType() == Material.AIR) {
					cached.put(id, error);
					inv.setItem(slots[n++], error);
					continue;
				}

				ItemMeta meta = item.getItemMeta();
				List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
				lore.add("");
				if(deleteMode)
				{
					lore.add(ChatColor.RED + AltChar.cross + " CLICK TO DELETE " + AltChar.cross);
					meta.setDisplayName(ChatColor.RED + "DELETE: " + meta.getDisplayName());
				}
				else
				{
					lore.add(ChatColor.YELLOW + AltChar.smallListDash + " Left click to obtain this item.");
					lore.add(ChatColor.YELLOW + AltChar.smallListDash + " Right click to edit this item.");
				}
				meta.setLore(lore);
				item.setItemMeta(meta);

				cached.put(id, item);
			}

			inv.setItem(slots[n++], cached.get(id));
		}

		ItemStack noItem = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
		ItemMeta noItemMeta = noItem.getItemMeta();
		noItemMeta.setDisplayName(ChatColor.RED + "- No Item -");
		noItem.setItemMeta(noItemMeta);

		ItemStack next = new ItemStack(Material.ARROW);
		ItemMeta nextMeta = next.getItemMeta();
		nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
		next.setItemMeta(nextMeta);

		ItemStack back = new ItemStack(deleteMode ? Material.BARRIER : Material.ARROW);
		ItemMeta backMeta = back.getItemMeta();
		backMeta.setDisplayName(deleteMode ? (ChatColor.RED + "Cancel") : (ChatColor.GREEN + AltChar.rightArrow + " Back"));
		back.setItemMeta(backMeta);

		ItemStack create = new ItemStack(VersionMaterial.GREEN_STAINED_GLASS_PANE.toItem());
		ItemMeta createMeta = create.getItemMeta();
		createMeta.setDisplayName(ChatColor.GREEN + "Create New");
		create.setItemMeta(createMeta);

		ItemStack delete = new ItemStack(VersionMaterial.RED_STAINED_GLASS_PANE.toItem());
		ItemMeta deleteMeta = delete.getItemMeta();
		deleteMeta.setDisplayName(ChatColor.RED + "Delete Item");
		delete.setItemMeta(deleteMeta);

		ItemStack previous = new ItemStack(Material.ARROW);
		ItemMeta previousMeta = previous.getItemMeta();
		previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
		previous.setItemMeta(previousMeta);

		while (n < slots.length)
			inv.setItem(slots[n++], noItem);
		if(!deleteMode)
		{
			inv.setItem(47, delete);
			inv.setItem(51, create);
		}
		inv.setItem(49, back);
		inv.setItem(18, page > 1 ? previous : null);
		inv.setItem(26, max >= itemIds.size() ? null : next);
		return inv;
	}

	public Type getType() {
		return type;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory())
			return;

		ItemStack item = event.getCurrentItem();
		if (MMOUtils.isPluginItem(item, false)) {
			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next Page")) {
				page++;
				open();
				return;
			}

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Previous Page")) {
				page--;
				open();
				return;
			}

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.rightArrow + " Back"))
				new ItemBrowser(player).open();

			if (item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Cancel"))
			{
				deleteMode = false;
				open();
			}
			
			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Create New"))
				new NewItemEdition(this).enable("Write in the chat the text you want.");

			if (type != null && item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Delete Item")) {
				deleteMode = true;
				open();
			}
			
			if (type == null && !item.getItemMeta().getDisplayName().equals(ChatColor.RED + "- No type -")) {
				Type type = MMOItems.plugin.getTypes().get(NBTItem.get(item).getString("typeId"));
				new ItemBrowser(player, type).open();
			}
		}

		String id = NBTItem.get(item).getString("MMOITEMS_ITEM_ID");
		if (id.equals(""))
			return;

		if (deleteMode) {
			Bukkit.getScheduler().runTask(MMOItems.plugin, () -> Bukkit.dispatchCommand(player, "mi delete " + type.getId() + " " + id));
			deleteMode = false;
			ItemStack newItem = new ItemStack(VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem());
			ItemMeta noItemMeta = newItem.getItemMeta();
			noItemMeta.setDisplayName(ChatColor.RED + "- No Item -");
			item.setType(newItem.getType());
			item.setItemMeta(noItemMeta);
			open();
		}
		else
		{
			if (event.getAction() == InventoryAction.PICKUP_ALL) {
				player.getInventory().addItem(removeLastLoreLines(item, 3));
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
			}

			if (event.getAction() == InventoryAction.PICKUP_HALF)
				new ItemEdition(player, type, id, removeLastLoreLines(item, 3)).open();
		}
	}

	private ItemStack removeLastLoreLines(ItemStack item, int amount) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.getLore();
		meta.setLore(lore.subList(0, lore.size() - amount));

		ItemStack item1 = item.clone();
		item1.setItemMeta(meta);
		return item1;
	}
}
