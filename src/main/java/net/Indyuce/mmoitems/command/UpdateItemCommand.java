package net.Indyuce.mmoitems.command;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.MMOItem;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.gui.edition.ItemUpdaterEdition;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;

public class UpdateItemCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return true;
		}

		if (!sender.hasPermission("mmoitems.update"))
			return true;

		Player player = (Player) sender;
		if (args.length < 1) {
			NBTItem item = MMOLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());

			// for items generated before 2.0
			if (!item.hasTag("MMOITEMS_ITEM_TYPE")) {
				sender.sendMessage(ChatColor.RED + "Could not update your item.");
				return true;
			}

			ItemStack newItem = MMOItems.plugin.getUpdater().getUpdated(item.getItem());
			if (newItem == null || newItem.getType() == Material.AIR) {
				sender.sendMessage(ChatColor.RED + "Could not update your item.");
				return true;
			}

			player.getInventory().setItemInMainHand(newItem);
			sender.sendMessage(ChatColor.YELLOW + "Successfully updated your item.");
			return true;
		}

		// toggles on/off item updater
		if (!player.hasPermission("mmoitems.admin")) {
			Message.NOT_ENOUGH_PERMS_COMMAND.format(ChatColor.RED).send(sender);
			return true;
		}
		if (args.length < 2) {
			player.sendMessage(ChatColor.RED + "Usage: /updateitem <type> <id> or /updateitem");
			return true;
		}

		Type type = Type.get(args[0]);
		if (type == null) {
			player.sendMessage(ChatColor.RED + "Please specify a valid item type.");
			return true;
		}

		MMOItem mmoitem = MMOItems.plugin.getItems().getMMOItem(type, args[1]);
		if (mmoitem == null) {
			player.sendMessage(ChatColor.RED + "This item does not exist or has issues loading.");
			return true;
		}

		new ItemUpdaterEdition(player, mmoitem).open();
		return true;
	}
}
