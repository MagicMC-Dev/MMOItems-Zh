package net.Indyuce.mmoitems.command.mmoitems.update;

import org.bukkit.command.CommandSender;

import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class UpdateCommandTreeNode extends CommandTreeNode {
	public UpdateCommandTreeNode(CommandTreeNode parent) {
		super(parent, "update");

		addChild(new ListCommandTreeNode(this));
		addChild(new ApplyCommandTreeNode(this));
		addChild(new InfoCommandTreeNode(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
