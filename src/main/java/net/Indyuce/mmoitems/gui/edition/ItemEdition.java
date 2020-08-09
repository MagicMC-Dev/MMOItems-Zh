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
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionMaterial;

public class ItemEdition extends EditionInventory {
	private static final int[] slots = { 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43 };

	public ItemEdition(Player player, MMOItem mmoitem) {
		super(player, mmoitem);
	}

	/*
	 * used in the item brower and when using the /mi edit command.
	 */
	public ItemEdition(Player player, MMOItem mmoitem, ItemStack cached) {
		super(player, mmoitem, cached);
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
		List<ItemStat> appliable = new ArrayList<>(getEdited().getType().getAvailableStats()).stream()
				.filter(stat -> stat.hasValidMaterial(getCachedItem()) && !(stat instanceof InternalStat)).collect(Collectors.toList());

		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Item Edition: " + getEdited().getId());
		for (int j = min; j < Math.min(appliable.size(), max); j++) {
			ItemStat stat = appliable.get(j);
			ItemStack item = stat.getItem();
			ItemMeta meta = item.getItemMeta();
			meta.addItemFlags(ItemFlag.values());
			meta.setDisplayName(ChatColor.GREEN + stat.getName());
			List<String> lore = new ArrayList<>();
			for (String s1 : stat.getLore())
				lore.add(ChatColor.GRAY + MMOLib.plugin.parseColors(s1));
			lore.add("");

			stat.whenDisplayed(lore, mmoitem);

			meta.setLore(lore);
			item.setItemMeta(meta);
			inv.setItem(slots[n++], MMOLib.plugin.getVersion().getWrapper().getNBTItem(item).addTag(new ItemTag("guiStat", stat.getId())).toItem());
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
		if (!MMOUtils.isMetaItem(item, false) || event.getInventory().getItem(4) == null)
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

	public ItemEdition onPage(int value) {
		page = value;
		return this;
	}
}