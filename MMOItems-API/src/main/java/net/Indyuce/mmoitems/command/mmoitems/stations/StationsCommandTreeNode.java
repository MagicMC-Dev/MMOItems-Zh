package net.Indyuce.mmoitems.command.mmoitems.stations;

import org.bukkit.command.CommandSender;

import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class StationsCommandTreeNode extends CommandTreeNode {
	public StationsCommandTreeNode(CommandTreeNode parent) {
		super(parent, "stations");

		addChild(new OpenCommandTreeNode(this));
		addChild(new ListCommandTreeNode(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
