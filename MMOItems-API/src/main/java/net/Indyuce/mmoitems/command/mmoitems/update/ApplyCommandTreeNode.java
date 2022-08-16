package net.Indyuce.mmoitems.command.mmoitems.update;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.PluginUpdate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;


public class ApplyCommandTreeNode extends CommandTreeNode {
	public ApplyCommandTreeNode(CommandTreeNode parent) {
		super(parent, "apply");

		addParameter(
				new Parameter("<id>", (explorer, list) -> MMOItems.plugin.getUpdates().getAll().forEach(update -> list.add("" + update.getId()))));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		int id;
		try {
			id = Integer.parseInt(args[2]);
		} catch (NumberFormatException exception) {
			sender.sendMessage(ChatColor.RED + "Please specify a valid number.");
			return CommandResult.FAILURE;
		}

		if (!MMOItems.plugin.getUpdates().has(id)) {
			sender.sendMessage(ChatColor.RED + "Could not find any config update with ID " + id);
			return CommandResult.FAILURE;
		}

		PluginUpdate update = MMOItems.plugin.getUpdates().get(id);
		sender.sendMessage(ChatColor.YELLOW + "Applying config update " + id + "...");
		update.apply(sender);
		sender.sendMessage(
				ChatColor.YELLOW + "Config update " + id + " was successfully applied. Check the console for potential update error logs.");
		return CommandResult.SUCCESS;
	}
}
