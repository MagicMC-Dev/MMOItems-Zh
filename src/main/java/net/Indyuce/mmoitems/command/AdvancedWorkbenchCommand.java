package net.Indyuce.mmoitems.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.gui.AdvancedWorkbench;

public class AdvancedWorkbenchCommand implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("mmoitems.awb"))
			return true;
		
		if(args.length < 1)
		{
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "You need to specify a player. '/awb [player]'");
				return true;
			}

			new AdvancedWorkbench((Player) sender).open();
			return true;
		}

		Player player = Bukkit.getPlayer(args[0]);
		if(player == null) {
			sender.sendMessage(ChatColor.RED + "Player '" + args[0] + "' not found.");
			return true;
		}

		new AdvancedWorkbench((Player) sender).open();
		return true;
	}
}
