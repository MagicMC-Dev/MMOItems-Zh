package net.Indyuce.mmoitems.command.mmoitems.update;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.PluginUpdate;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;

public class InfoCommandTreeNode extends CommandTreeNode {
	public InfoCommandTreeNode(CommandTreeNode parent) {
		super(parent, "info");

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

		sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Config Update n" + update.getId());
		if (update.hasDescription()) {
			sender.sendMessage("");
			sender.sendMessage(ChatColor.DARK_GRAY + "Description:");
			for (String line : update.getDescription())
				sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.translateAlternateColorCodes('&', line));
		}

		sender.sendMessage("");
		sender.sendMessage(ChatColor.YELLOW + "Use " + ChatColor.GOLD + "/mi update apply " + update.getId() + ChatColor.YELLOW
				+ " to apply this config update.");
		return CommandResult.SUCCESS;
	}
}
