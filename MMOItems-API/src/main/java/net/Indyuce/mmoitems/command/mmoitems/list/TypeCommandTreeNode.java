package net.Indyuce.mmoitems.command.mmoitems.list;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class TypeCommandTreeNode extends CommandTreeNode {
	public TypeCommandTreeNode(CommandTreeNode parent) {
		super(parent, "type");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " Item Types "
				+ ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + ChatColor.LIGHT_PURPLE + " Item Types " + ChatColor.DARK_GRAY + ""
				+ ChatColor.STRIKETHROUGH + "]-----------------");
		for (Type type : MMOItems.plugin.getTypes().getAll())
			sender.sendMessage("* " + ChatColor.LIGHT_PURPLE + type.getName() + " (" + type.getId() + ")");
		return CommandResult.SUCCESS;
	}
}
