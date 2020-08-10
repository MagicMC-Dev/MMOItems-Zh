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
import net.Indyuce.mmoitems.api.UpdaterData;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.manager.UpdaterManager.KeepOption;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class ItemUpdaterEdition extends EditionInventory {
	private static final int[] slots = { 19, 20, 21, 28, 29, 30, 37, 38, 39 };

	public ItemUpdaterEdition(Player player, MMOItemTemplate template) {
		super(player, template);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Item Updater: " + template.getId());

		// setup if not in map
		if (!MMOItems.plugin.getUpdater().hasData(template)) {
			MMOItems.plugin.getUpdater().enable(template);
			player.sendMessage(ChatColor.YELLOW + "Successfully enabled the item updater for " + template.getId() + ".");
		}

		UpdaterData did = MMOItems.plugin.getUpdater().getData(template);

		ItemStack disable = VersionMaterial.RED_STAINED_GLASS_PANE.toItem();
		ItemMeta disableMeta = disable.getItemMeta();
		disableMeta.setDisplayName(ChatColor.GREEN + "Disable");
		List<String> disableLore = new ArrayList<String>();
		disableLore.add(ChatColor.GRAY + "Your item will not be dynamically updated.");
		disableLore.add("");
		disableLore.add(ChatColor.YELLOW + AltChar.listDash + " Click to disable the item updater.");
		disableMeta.setLore(disableLore);
		disable.setItemMeta(disableMeta);

		int n = 0;
		for (KeepOption option : KeepOption.values())
			inv.setItem(slots[n++],
					getBooleanItem(MMOUtils.caseOnWords(option.name().substring(5).toLowerCase()), did.hasOption(option), option.getLore()));

		inv.setItem(32, disable);
		inv.setItem(4, getCachedItemStack());

		return inv;
	}

	private ItemStack getBooleanItem(String name, boolean bool, List<String> list) {
		ItemStack stack = (bool ? VersionMaterial.LIME_DYE : VersionMaterial.GRAY_DYE).toItem();
		ItemMeta meta = stack.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Keep " + name + "?");
		List<String> lore = new ArrayList<>();
		list.forEach(line -> lore.add(ChatColor.GRAY + line));
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
		if (!MMOItems.plugin.getUpdater().hasData(template)) {
			player.closeInventory();
			return;
		}

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Disable")) {
			MMOItems.plugin.getUpdater().disable(template);
			player.closeInventory();
			player.sendMessage(ChatColor.YELLOW + "Successfully disabled the item updater for " + template.getId() + ".");
			return;
		}

		/*
		 * find clicked option based on item display name. no need to use
		 * NBTTags
		 */
		String format = item.getItemMeta().getDisplayName();
		if (!format.startsWith(ChatColor.GREEN + "Keep "))
			return;

		UpdaterData did = MMOItems.plugin.getUpdater().getData(template);
		KeepOption option = KeepOption.valueOf(format.substring(2, format.length() - 1).replace(" ", "_").toUpperCase());
		if (did.hasOption(option))
			did.removeOption(option);
		else
			did.addOption(option);

		open();
	}
}