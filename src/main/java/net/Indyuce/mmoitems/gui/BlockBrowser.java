package net.Indyuce.mmoitems.gui;

import java.util.ArrayList;
import java.util.Arrays;
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
import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.mmogroup.mmolib.api.util.AltChar;
import net.Indyuce.mmoitems.gui.edition.BlockEdition;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class BlockBrowser extends PluginInventory {
	private Map<CustomBlock, ItemStack> cached = new HashMap<>();

	private static final int[] slots = { 10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34 };

	public BlockBrowser(Player player) {
		super(player);
	}

	@Override
	public Inventory getInventory() {
		int min = (page - 1) * slots.length;
		int max = page * slots.length;
		int n = 0;

		ItemStack error = VersionMaterial.RED_STAINED_GLASS_PANE.toItem();
		ItemMeta errorMeta = error.getItemMeta();
		errorMeta.setDisplayName(ChatColor.RED + "- Error -");
		List<String> errorLore = new ArrayList<>();
		errorLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "An error occured while");
		errorLore.add(ChatColor.GRAY + "" + ChatColor.ITALIC + "trying to generate that block.");
		errorMeta.setLore(errorLore);
		error.setItemMeta(errorMeta);

		List<CustomBlock> blocks = new ArrayList<>(MMOItems.plugin.getCustomBlocks().getAll());

		/*
		 * displays every item in a specific type. items are cached inside the
		 * map at the top to reduce performance impact and are directly rendered
		 */
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Block Explorer");
		for (int j = min; j < Math.min(max, blocks.size()); j++) {
			CustomBlock block = blocks.get(j);
			if (!cached.containsKey(block)) {
				ItemStack item = block.getItem();
				if (item == null || item.getType() == Material.AIR) {
					cached.put(block, error);
					inv.setItem(slots[n++], error);
					continue;
				}

				ItemMeta meta = item.getItemMeta();
				List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
				lore.add("");
				lore.add(ChatColor.YELLOW + AltChar.smallListDash + " Left click to obtain this block.");
				lore.add(ChatColor.YELLOW + AltChar.smallListDash + " Right click to edit this block.");
				meta.setLore(lore);
				item.setItemMeta(meta);

				cached.put(block, item);
			}

			inv.setItem(slots[n++], cached.get(block));
		}

		ItemStack noItem = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
		ItemMeta noItemMeta = noItem.getItemMeta();
		noItemMeta.setDisplayName(ChatColor.RED + "- No Block -");
		noItem.setItemMeta(noItemMeta);

		ItemStack next = new ItemStack(Material.ARROW);
		ItemMeta nextMeta = next.getItemMeta();
		nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
		next.setItemMeta(nextMeta);

		ItemStack previous = new ItemStack(Material.ARROW);
		ItemMeta previousMeta = previous.getItemMeta();
		previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
		previous.setItemMeta(previousMeta);

		ItemStack switchBrowse = new ItemStack(Material.IRON_SWORD);
		ItemMeta switchMeta = switchBrowse.getItemMeta();
		switchMeta.addItemFlags(ItemFlag.values());
		switchMeta.setDisplayName(ChatColor.GREEN + "Switch to Item Explorer");
		switchBrowse.setItemMeta(switchMeta);

		ItemStack downloadPack = new ItemStack(Material.HOPPER);
		ItemMeta downloadMeta = downloadPack.getItemMeta();
		downloadMeta.setDisplayName(ChatColor.GREEN + "Download Default Resourcepack");
		downloadMeta.setLore(Arrays.asList(ChatColor.LIGHT_PURPLE + "Only seeing clay balls?", "", ChatColor.RED + "By downloading the default resourcepack you can", ChatColor.RED + "edit the blocks however you want.", ChatColor.RED + "You will still have to add it to your server!"));
		downloadPack.setItemMeta(downloadMeta);
		
		while (n < slots.length)
			inv.setItem(slots[n++], noItem);
		inv.setItem(18, page > 1 ? previous : null);
		inv.setItem(26, max >= blocks.size() ? null : next);
		
		inv.setItem(45, downloadPack);
		inv.setItem(53, switchBrowse);
		
		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory())
			return;

		ItemStack item = event.getCurrentItem();
		if (item == null) return;
		if (MMOUtils.isMetaItem(item, false)) {
			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next Page")) {
				page++; open(); return;
			}

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Previous Page")) {
				page--; open(); return;
			}

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Switch to Item Explorer")) {
				new ItemBrowser(player).open(); return;
			}

			if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Download Default Resourcepack")) {
				MMOLib.plugin.getVersion().getWrapper().sendJson(player, "[{\"text\":\"Click to download!\",\"color\":\"green\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://mythiccraft.io/resources/MICustomBlockPack.zip\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":[\"\",{\"text\":\"https://mythiccraft.io/resources/MICustomBlockPack.zip\",\"italic\":true,\"color\":\"white\"}]}}]");
				player.closeInventory();
				return;
			}
		}
		if(item.getType() == Material.CLAY_BALL) {
			if (event.getAction() == InventoryAction.PICKUP_ALL) {
				player.getInventory().addItem(removeLastLoreLines(item, 3));
				player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
			}
			if (event.getAction() == InventoryAction.PICKUP_HALF)
				new BlockEdition(player, MMOItems.plugin.getCustomBlocks().getBlock(NBTItem.get(item).getInteger("MMOITEMS_BLOCK_ID"))).open();
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
