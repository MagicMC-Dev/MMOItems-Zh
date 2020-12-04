package net.Indyuce.mmoitems.command.mmoitems.revid;

import net.Indyuce.mmoitems.MMOItems;
import net.mmogroup.mmolib.command.api.CommandTreeNode;
import net.mmogroup.mmolib.command.api.Parameter;
import org.bukkit.command.CommandSender;

public class RevisionIDCommandTreeNode extends CommandTreeNode {
	public static final Parameter TYPE_OR_ALL = new Parameter("<type>", (explorer, list) -> {
		MMOItems.plugin.getTypes().getAll().forEach(type -> list.add(type.getId()));
		list.add("ALL");
	});

	public RevisionIDCommandTreeNode(CommandTreeNode parent) {
		super(parent, "revid");

		addChild(new RevIncreaseCommandTreeNode(this));
		addChild(new RevDecreaseCommandTreeNode(this));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
