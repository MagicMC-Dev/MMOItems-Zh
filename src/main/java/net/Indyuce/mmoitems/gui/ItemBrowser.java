package net.Indyuce.mmoitems.gui;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.edition.NewItemEdition;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemBrowser extends PluginInventory {
	private Map<String, ItemStack> cached = new LinkedHashMap<>();

	private final Type type;
	private boolean deleteMode;

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
			Inventory inv = Bukkit.createInventory(this, 54, "Item Explorer");
			List<Type> types = new ArrayList<>(MMOItems.plugin.getTypes().getAll());
			for (int j = min; j < Math.min(max, types.size()); j++) {
				Type type = types.get(j);
				int items = MMOItems.plugin.getTemplates().getTemplates(type).size();

				ItemStack item = type.getItem();
				item.setAmount(Math.max(1, Math.min(64, items)));
				ItemMeta meta = item.getItemMeta();
				meta.setDisplayName(ChatColor.GREEN + type.getName() + ChatColor.DARK_GRAY + " (Click to browse)");
				meta.addItemFlags(ItemFlag.values());
				List<String> lore = new ArrayList<>();
				lore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "There " + (items != 1 ? "are" : "is") + " "
						+ (items < 1 ? "" + ChatColor.RED + ChatColor.ITALIC + "no" : "" + ChatColor.GOLD + ChatColor.ITALIC + items) + ChatColor.GRAY
						+ ChatColor.ITALIC + " item" + (items != 1 ? "s" : "") + " in that type.");
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
			inv.setItem(26, max >= MMOItems.plugin.getTypes().getAll().size() ? null : next);

			return inv;
		}

		ItemStack error = VersionMaterial.RED_STAINED_GLASS_PANE.toItem();
		ItemMeta errorMeta = error.getItemMeta();
		errorMeta.setDisplayName(ChatColor.RED + "- Error -");
		List<String> errorLore = new ArrayList<>();
		errorLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "An error occurred while");
		errorLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "trying to generate that item.");
		errorMeta.setLore(errorLore);
		error.setItemMeta(errorMeta);

		List<MMOItemTemplate> templates = new ArrayList<>(MMOItems.plugin.getTemplates().getTemplates(type));

		/*
		 * displays every item in a specific type. items are cached inside the
		 * map at the top to reduce performance impact and are directly rendered
		 */
		Inventory inv = Bukkit.createInventory(this, 54, (deleteMode ? ("Delete Mode: ") : ("Item Explorer: ")) + type.getName());
		for (int j = min; j < Math.min(max, templates.size()); j++) {
			MMOItemTemplate template = templates.get(j);
			ItemStack item = template.newBuilder(getPlayerData().getRPG()).build().newBuilder().build();
			if (item == null || item.getType() == Material.AIR) {
				cached.put(template.getId(), error);
				inv.setItem(slots[n++], error);
				continue;
			}
			NBTItem nbtItem = NBTItem.get(item);

			List<BaseComponent> newLore = new ArrayList<>();
			newLore.add(toComponent(""));
			if (deleteMode) {
				newLore.add(toComponent(ChatColor.RED + AltChar.cross + " CLICK TO DELETE " + AltChar.cross));

				BaseComponent display = nbtItem.getDisplayNameComponent();
				if (display.getExtra() != null) {
					List<BaseComponent> extra = new ArrayList<>(display.getExtra());
					extra.add(0, toComponent(ChatColor.RED + "DELETE: "));
					display.setExtra(extra);
					nbtItem.setDisplayNameComponent(display);
				}

			} else {
				newLore.add(toComponent(ChatColor.YELLOW + AltChar.smallListDash + " Left click to obtain this item."));
				newLore.add(toComponent(ChatColor.YELLOW + AltChar.smallListDash + " Right click to edit this item."));
			}

			List<BaseComponent> lore = nbtItem.getLoreComponents();
			lore.addAll(newLore);
			nbtItem.setLoreComponents(lore);

			cached.put(template.getId(), nbtItem.toItem());

			inv.setItem(slots[n++], cached.get(template.getId()));
		}

		ItemStack noItem = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
		ItemMeta noItemMeta = noItem.getItemMeta();
		noItemMeta.setDisplayName(ChatColor.RED + "- No Item -");
		noItem.setItemMeta(noItemMeta);

		ItemStack next = new ItemStack(Material.ARROW);
		ItemMeta nextMeta = next.getItemMeta();
		nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
		next.setItemMeta(nextMeta);

		ItemStack back = new ItemStack(Material.ARROW);
		ItemMeta backMeta = back.getItemMeta();
		backMeta.setDisplayName(ChatColor.GREEN + AltChar.rightArrow + " Back");
		back.setItemMeta(backMeta);

		ItemStack create = new ItemStack(VersionMaterial.WRITABLE_BOOK.toMaterial());
		ItemMeta createMeta = create.getItemMeta();
		createMeta.setDisplayName(ChatColor.GREEN + "Create New");
		create.setItemMeta(createMeta);

		ItemStack delete = new ItemStack(VersionMaterial.CAULDRON.toMaterial());
		ItemMeta deleteMeta = delete.getItemMeta();
		deleteMeta.setDisplayName(ChatColor.RED + (deleteMode ? "Cancel Deletion" : "Delete Item"));
		delete.setItemMeta(deleteMeta);

		ItemStack previous = new ItemStack(Material.ARROW);
		ItemMeta previousMeta = previous.getItemMeta();
		previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
		previous.setItemMeta(previousMeta);

		if (type == Type.BLOCK) {
			ItemStack downloadPack = new ItemStack(Material.HOPPER);
			ItemMeta downloadMeta = downloadPack.getItemMeta();
			downloadMeta.setDisplayName(ChatColor.GREEN + "Download Default Resourcepack");
			downloadMeta.setLore(Arrays.asList(ChatColor.LIGHT_PURPLE + "Only seeing stone blocks?", "",
					ChatColor.RED + "By downloading the default resourcepack you can", ChatColor.RED + "edit the blocks however you want.",
					ChatColor.RED + "You will still have to add it to your server!"));
			downloadPack.setItemMeta(downloadMeta);
			inv.setItem(45, downloadPack);
		}

		while (n < slots.length)
			inv.setItem(slots[n++], noItem);
		if (!deleteMode)
			inv.setItem(51, create);
		inv.setItem(47, delete);
		inv.setItem(49, back);
		inv.setItem(18, page > 1 ? previous : null);
		inv.setItem(26, max >= templates.size() ? null : next);
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
		if (MMOUtils.isMetaItem(item, false)) {
			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next Page")) {
				page++;
				open();
			}

			else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Previous Page")) {
				page--;
				open();
			}

			else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + AltChar.rightArrow + " Back"))
				new ItemBrowser(getPlayer()).open();

			else if (item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Cancel Deletion")) {
				deleteMode = false;
				open();
			}

			else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Create New"))
				new NewItemEdition(this).enable("Write in the chat the text you want.");

			else if (type != null && item.getItemMeta().getDisplayName().equals(ChatColor.RED + "Delete Item")) {
				deleteMode = true;
				open();
			}

			else if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Download Default Resourcepack")) {
				MMOLib.plugin.getVersion().getWrapper().sendJson(getPlayer(),
						"[{\"text\":\"Click to download!\",\"color\":\"green\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://drive.google.com/uc?id=1FjV7y-2cn8qzSiktZ2CUXmkdjepXdj5N\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":[\"\",{\"text\":\"https://drive.google.com/uc?id=1FjV7y-2cn8qzSiktZ2CUXmkdjepXdj5N\",\"italic\":true,\"color\":\"white\"}]}}]");
				getPlayer().closeInventory();
			}

			else if (type == null && !item.getItemMeta().getDisplayName().equals(ChatColor.RED + "- No type -"))
				new ItemBrowser(getPlayer(), MMOItems.plugin.getTypes().get(NBTItem.get(item).getString("typeId"))).open();
		}

		String id = NBTItem.get(item).getString("MMOITEMS_ITEM_ID");
		if (id.equals(""))
			return;

		if (deleteMode) {
			MMOItems.plugin.getTemplates().deleteTemplate(type, id);
			deleteMode = false;
			open();

		} else {
			if (event.getAction() == InventoryAction.PICKUP_ALL) {
				// this refreshes the item if it's unstackable
				ItemStack generatedItem = (NBTItem.get(item).getBoolean("UNSTACKABLE")) ? MMOItems.plugin.getItem(type, id, playerData)
						: removeLastLoreLines(NBTItem.get(item));
				getPlayer().getInventory().addItem(generatedItem);
				getPlayer().playSound(getPlayer().getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
			}

			if (event.getAction() == InventoryAction.PICKUP_HALF)
				new ItemEdition(getPlayer(), MMOItems.plugin.getTemplates().getTemplate(type, id)).open();
		}
	}

	private ItemStack removeLastLoreLines(NBTItem item) {
		List<BaseComponent> lore = item.getLoreComponents();
		item.setLoreComponents(lore.subList(0, lore.size() - 3));
		return item.toItem();
	}
	private BaseComponent toComponent(String text) {
		BaseComponent component = TextComponent.fromLegacyText(text)[0];
		component.setItalic(false);
		return component;
	}
}
