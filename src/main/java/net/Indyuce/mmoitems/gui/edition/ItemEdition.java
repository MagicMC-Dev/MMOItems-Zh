package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.version.VersionMaterial;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class ItemEdition extends EditionInventory {
	private static final int[] slots = { 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43 };

	public ItemEdition(Player player, Type type, String id) {
		super(player, type, id);
	}

	/*
	 * used in the item brower and when using the /mi edit command.
	 */
	public ItemEdition(Player player, Type type, String id, ItemStack cached) {
		super(player, type, id, cached);
	}

	@Override
	public Inventory getInventory() {
		int min = (page - 1) * slots.length;
		int max = page * slots.length;
		int n = 0;

		/*
		 * it has to determin what stats can be applied first because otherwise
		 * the for loop will just let some slots empty
		 */
		List<ItemStat> appliable = new ArrayList<>(type.getAvailableStats()).stream().filter(stat -> stat.hasValidMaterial(getCachedItem()) && !stat.isInternal()).collect(Collectors.toList());

		ConfigFile config = type.getConfigFile();
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Item Edition: " + id);
		for (int j = min; j < Math.min(appliable.size(), max); j++) {
			ItemStat stat = appliable.get(j);
			ItemStack item = stat.getItem();
			ItemMeta meta = item.getItemMeta();
			meta.addItemFlags(ItemFlag.values());
			meta.setDisplayName(ChatColor.GREEN + stat.getName());
			List<String> lore = new ArrayList<>();
			for (String s1 : stat.getLore())
				lore.add(ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', s1));

			stat.whenDisplayed(lore, config.getConfig(), id);

			meta.setLore(lore);
			item.setItemMeta(meta);
			inv.setItem(slots[n++], MMOItems.plugin.getNMS().getNBTItem(item).addTag(new ItemTag("guiStat", stat.getId())).toItem());
		}

		ItemStack glass = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
		ItemMeta glassMeta = glass.getItemMeta();
		glassMeta.setDisplayName(ChatColor.RED + "- No Item Stat -");
		glass.setItemMeta(glassMeta);

		ItemStack next = new ItemStack(Material.ARROW);
		ItemMeta nextMeta = next.getItemMeta();
		nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
		next.setItemMeta(nextMeta);

		ItemStack previous = new ItemStack(Material.ARROW);
		ItemMeta previousMeta = previous.getItemMeta();
		previousMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
		previous.setItemMeta(previousMeta);

		addEditionInventoryItems(inv, true);

		while (n < slots.length)
			inv.setItem(slots[n++], glass);
		inv.setItem(27, page > 1 ? previous : null);
		inv.setItem(35, appliable.size() > max ? next : null);

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory())
			return;

		ItemStack item = event.getCurrentItem();
		if (!MMOUtils.isPluginItem(item, false) || event.getInventory().getItem(4) == null)
			return;

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Next Page")) {
			page++;
			open();
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Previous Page")) {
			page--;
			open();
		}

		String tag = NBTItem.get(item).getString("guiStat");
		if (!tag.equals(""))
			MMOItems.plugin.getStats().get(tag).whenClicked(this, event);
	}
}