package net.Indyuce.mmoitems.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.gui.AdvancedWorkbench;

public class AdvancedWorkbenchCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return true;
		}

		if (!sender.hasPermission("mmoitems.awb"))
			return true;

		new AdvancedWorkbench((Player) sender).open();
		return true;
	}
}
