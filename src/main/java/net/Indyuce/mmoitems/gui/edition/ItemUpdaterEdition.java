package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.util.AltChar;
import net.Indyuce.mmoitems.manager.UpdaterManager.UpdaterData;
import net.mmogroup.mmolib.version.VersionMaterial;

public class ItemUpdaterEdition extends EditionInventory {
	public ItemUpdaterEdition(Player player, Type type, String id) {
		super(player, type, id);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Item Updater: " + id);

		// setup if not in map
		String itemPath = type.getId() + "." + id;
		if (!MMOItems.plugin.getUpdater().hasData(itemPath)) {
			MMOItems.plugin.getUpdater().enable(itemPath);
			player.sendMessage(ChatColor.YELLOW + "Successfully enabled the item updater for " + id + ".");
		}

		UpdaterData did = MMOItems.plugin.getUpdater().getData(itemPath);

		ItemStack disable = VersionMaterial.RED_STAINED_GLASS_PANE.toItem();
		ItemMeta disableMeta = disable.getItemMeta();
		disableMeta.setDisplayName(ChatColor.GREEN + "Disable");
		List<String> disableLore = new ArrayList<String>();
		disableLore.add(ChatColor.GRAY + "Your item won't update anymore.");
		disableLore.add("");
		disableLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to disable the item updater.");
		disableMeta.setLore(disableLore);
		disable.setItemMeta(disableMeta);

		inv.setItem(20, getBooleanItem("Name", did.keepName(), "Your item will keep its", "old display name when updated."));
		inv.setItem(21, getBooleanItem("Lore", did.keepLore(), "Any lore starting with '&7' will be", "kept when updating your item.", ChatColor.RED + "May not support every enchant plugin."));
		inv.setItem(29, getBooleanItem("Gems", did.keepGems(), "Your item will keep its", "old gems when updated."));
		inv.setItem(30, getBooleanItem("Enchants", did.keepEnchants(), "Your item will keep its", "old enchants when updated."));
		inv.setItem(38, getBooleanItem("Soulbound", did.keepSoulbound(), "Your item will keep its", "soulbound data when updated."));
		inv.setItem(39, getBooleanItem("DefaultDurability", did.keepDurability(), "Your item will keep its", "old durability when updated."));
		inv.setItem(32, disable);

		inv.setItem(4, getCachedItem());

		return inv;
	}

	private ItemStack getBooleanItem(String name, boolean bool, String... lines) {
		ItemStack stack = (bool ? VersionMaterial.LIME_DYE : VersionMaterial.GRAY_DYE).toItem();
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Keep " + name + "?");
		List<String> lore = new ArrayList<String>();
		for (String line : lines)
			lore.add(ChatColor.GRAY + line);
		lore.add("");
		lore.add(bool ? ChatColor.RED + AltChar.listDash + " Click to toggle off." : ChatColor.GREEN + AltChar.listDash + " Click to toggle on.");
		meta.setLore(lore);
		stack.setItemMeta(meta);

		return stack;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
			return;

		// safe check
		String path = type.getId() + "." + id;
		if (!MMOItems.plugin.getUpdater().hasData(path)) {
			player.closeInventory();
			return;
		}

		UpdaterData did = MMOItems.plugin.getUpdater().getData(path);
		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Keep Lore?")) {
			did.setKeepLore(!did.keepLore());
			open();
		}
		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Keep Enchants?")) {
			did.setKeepEnchants(!did.keepEnchants());
			open();
		}
		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Keep DefaultDurability?")) {
			did.setKeepDurability(!did.keepDurability());
			open();
		}
		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Keep Name?")) {
			did.setKeepName(!did.keepName());
			open();
		}
		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Keep Gems?")) {
			did.setKeepGems(!did.keepGems());
			open();
		}
		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Keep Soulbound?")) {
			did.setKeepSoulbound(!did.keepSoulbound());
			open();
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Disable")) {
			MMOItems.plugin.getUpdater().disable(path);
			player.closeInventory();
			player.sendMessage(ChatColor.YELLOW + "Successfully disabled the item updater for " + id + ".");
		}
	}
}