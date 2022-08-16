package net.Indyuce.mmoitems.command.mmoitems.revid;

import net.Indyuce.mmoitems.MMOItems;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import org.bukkit.command.CommandSender;

public class RevisionIDCommandTreeNode extends CommandTreeNode {
	public static final Parameter TYPE_OR_ALL = new Parameter("<type>", (explorer, list) -> {
		MMOItems.plugin.getTypes().getAll().forEach(type -> list.add(type.getId()));
		list.add("ALL");
	});

	public RevisionIDCommandTreeNode(CommandTreeNode parent) {
		super(parent, "revid");

		addChild(new RevIDActionCommandTreeNode(this, "increase", (revId) -> Math.min(revId + 1, Integer.MAX_VALUE)));
		addChild(new RevIDActionCommandTreeNode(this, "decrease", (revId) -> Math.max(revId - 1, 1)));


	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		return CommandResult.THROW_USAGE;
	}
}
