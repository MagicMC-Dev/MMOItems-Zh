package net.Indyuce.mmoitems.command.mmoitems.debug;

import org.bukkit.command.CommandSender;

import net.mmogroup.mmolib.command.api.CommandTreeNode;

public class DebugCommandTreeNode extends CommandTreeNode {
	public DebugCommandTreeNode(CommandTreeNode parent) {
		super(parent, "debug");

		addChild(new CheckStatCommandTreeNode(this));
		addChild(new CheckAttributeCommandTreeNode(this));
		addChild(new CheckTagCommandTreeNode(this));
		addChild(new SetTagCommandTreeNode(this));
		addChild(new CheckTagsCommandTreeNode(this));
		addChild(new InfoCommandTreeNode(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
