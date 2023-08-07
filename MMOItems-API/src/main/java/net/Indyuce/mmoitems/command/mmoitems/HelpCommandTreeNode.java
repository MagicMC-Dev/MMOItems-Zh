package net.Indyuce.mmoitems.command.mmoitems;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import net.Indyuce.mmoitems.command.PluginHelp;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HelpCommandTreeNode extends CommandTreeNode {
	public HelpCommandTreeNode(CommandTreeNode parent) {
		super(parent, "help");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 2) {
			new PluginHelp(sender).open(1);
			return CommandResult.SUCCESS;
		}

		try {
			new PluginHelp(sender).open(Integer.parseInt(args[1]));
			return CommandResult.SUCCESS;

		} catch (NumberFormatException exception) {
			sender.sendMessage(ChatColor.RED + args[1] + " is not a valid number.");
			return CommandResult.FAILURE;
		}
	}
}
