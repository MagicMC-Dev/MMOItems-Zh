package net.Indyuce.mmoitems.command.mmoitems.stations;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.CraftingStation;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class ListCommandTreeNode extends CommandTreeNode {
	public ListCommandTreeNode(CommandTreeNode parent) {
		super(parent, "list");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " Crafting Stations "
				+ ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "]-----------------");
		for (CraftingStation station : MMOItems.plugin.getCrafting().getAll())
			sender.sendMessage(ChatColor.GRAY + "- " + ChatColor.WHITE + station.getId());
		return CommandResult.SUCCESS;
	}
}
