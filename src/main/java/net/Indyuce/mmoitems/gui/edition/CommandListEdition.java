package net.Indyuce.mmoitems.gui.edition;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.api.util.AltChar;
import net.mmogroup.mmolib.version.VersionMaterial;

public class CommandListEdition extends EditionInventory {
	private static final int[] slots = { 19, 20, 21, 22, 23, 24, 25, 28, 29, 33, 34, 37, 38, 42, 43 };

	public CommandListEdition(Player player, MMOItem mmoitem) {
		super(player, mmoitem);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, 54, ChatColor.UNDERLINE + "Command List");
		int n = 0;

		FileConfiguration config = mmoitem.getType().getConfigFile().getConfig();

		if (config.getConfigurationSection(mmoitem.getId()).contains("commands"))
			for (String key : config.getConfigurationSection(mmoitem.getId() + ".commands").getKeys(false)) {

				String format = config.getString(mmoitem.getId() + ".commands." + key + ".format");
				double delay = config.getDouble(mmoitem.getId() + ".commands." + key + ".delay");
				boolean console = config.getBoolean(mmoitem.getId() + ".commands." + key + ".console"),
						op = config.getBoolean(mmoitem.getId() + ".commands." + key + ".op");

				ItemStack item = new ItemStack(VersionMaterial.COMPARATOR.toMaterial());
				ItemMeta itemMeta = item.getItemMeta();
				itemMeta.setDisplayName(format == null || format.equals("") ? ChatColor.RED + "No Format" : ChatColor.GREEN + format);
				List<String> itemLore = new ArrayList<>();
				itemLore.add("");
				itemLore.add(ChatColor.GRAY + "Command Delay: " + ChatColor.RED + delay);
				itemLore.add(ChatColor.GRAY + "Sent by Console: " + ChatColor.RED + console);
				itemLore.add(ChatColor.GRAY + "Sent w/ OP perms: " + ChatColor.RED + op);
				itemLore.add("");
				itemLore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove.");
				itemMeta.setLore(itemLore);
				item.setItemMeta(itemMeta);

				inv.setItem(slots[n++], NBTItem.get(item).addTag(new ItemTag("configKey", key)).toItem());
			}

		ItemStack glass = VersionMaterial.GRAY_STAINED_GLASS_PANE.toItem();
		ItemMeta glassMeta = glass.getItemMeta();
		glassMeta.setDisplayName(ChatColor.RED + "- No Command -");
		glass.setItemMeta(glassMeta);

		ItemStack add = new ItemStack(VersionMaterial.WRITABLE_BOOK.toMaterial());
		ItemMeta addMeta = add.getItemMeta();
		addMeta.setDisplayName(ChatColor.GREEN + "Register a command...");
		add.setItemMeta(addMeta);

		inv.setItem(40, add);
		while (n < slots.length)
			inv.setItem(slots[n++], glass);
		addEditionInventoryItems(inv, true);

		return inv;
	}

	@Override
	public void whenClicked(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();

		event.setCancelled(true);
		if (event.getInventory() != event.getClickedInventory() || !MMOUtils.isMetaItem(item, false))
			return;

		if (item.getItemMeta().getDisplayName().equals(ChatColor.GREEN + "Register a command...")) {
			new StatEdition(this, ItemStat.COMMANDS).enable("Write in the chat the command you want to add.", "", "To add a delay, use &c-d:<delay>",
					"To make the command cast itself w/ console, use &c-c", "To make the command cast w/ OP perms, use &c-op", "",
					"&eEx: -d:10.3 -op bc Hello, this is a test command.");
			return;
		}

		String tag = MMOLib.plugin.getNMS().getNBTItem(item).getString("configKey");
		if (tag.equals(""))
			return;

		if (event.getAction() == InventoryAction.PICKUP_HALF) {
			ConfigFile config = mmoitem.getType().getConfigFile();
			if (config.getConfig().getConfigurationSection(mmoitem.getId()).contains("commands")
					&& config.getConfig().getConfigurationSection(mmoitem.getId() + ".commands").contains(tag)) {
				config.getConfig().set(mmoitem.getId() + ".commands." + tag, null);
				registerItemEdition(config);
				open();
				player.sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + ChatColor.GOLD + tag + ChatColor.DARK_GRAY
						+ " (Internal ID)" + ChatColor.GRAY + ".");
			}
		}
	}
}