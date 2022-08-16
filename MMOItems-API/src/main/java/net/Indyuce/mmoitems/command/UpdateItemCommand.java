package net.Indyuce.mmoitems.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UpdateItemCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return true;
		}

		if (!sender.hasPermission("mmoitems.update")) return true;

		Player player = (Player) sender;
		if (args.length < 1 || !player.hasPermission("mmoitems.admin")) {
//			NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());

			// for items generated before 2.0
			/*if (!item.hasTag("MMOITEMS_ITEM_TYPE")) {
				sender.sendMessage(ChatColor.RED + "Could not update your item.");
				return true;
			}

			ItemStack newItem = MMOItems.plugin.getUpdater().getUpdated(item.getItem(), player);
			if (newItem == null || newItem.getType() == Material.AIR) {
				sender.sendMessage(ChatColor.RED + "Could not update your item.");
				return true;
			}

			player.getInventory().setItemInMainHand(newItem);
			sender.sendMessage(ChatColor.YELLOW + "Successfully updated your item.");
			return true;*/
		}

		/*
		 * TODO Cleanup
		 * Commented this out to reuse some of the code, bear with it until this system has been cleaned!
		 */
		// toggles on/off item updater
		/*if () {
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

		if (!MMOItems.plugin.getTemplates().hasTemplate(type, args[1])) {
			player.sendMessage(ChatColor.RED + "Could not find an item template with ID '" + args[1] + "'");
			return true;
		}*/

		return true;
	}
}
