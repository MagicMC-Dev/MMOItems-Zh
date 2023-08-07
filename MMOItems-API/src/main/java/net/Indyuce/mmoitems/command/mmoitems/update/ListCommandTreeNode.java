package net.Indyuce.mmoitems.command.mmoitems.update;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.PluginUpdate;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class ListCommandTreeNode extends CommandTreeNode {
	public ListCommandTreeNode(CommandTreeNode parent) {
		super(parent, "list");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.LIGHT_PURPLE + "Available Config Updates");
		for (PluginUpdate update : MMOItems.plugin.getUpdates().getAll())
			sender.sendMessage(ChatColor.DARK_GRAY + "- Update " + update.getId());
		return CommandResult.SUCCESS;
	}
}
