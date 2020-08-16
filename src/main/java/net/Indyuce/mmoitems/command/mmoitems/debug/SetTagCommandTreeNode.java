package net.Indyuce.mmoitems.command.mmoitems.debug;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.ItemTag;
import net.mmogroup.mmolib.command.api.CommandTreeNode;
import net.mmogroup.mmolib.command.api.Parameter;

public class SetTagCommandTreeNode extends CommandTreeNode {
	public SetTagCommandTreeNode(CommandTreeNode parent) {
		super(parent, "settag");

		addParameter(new Parameter("<path>", (explorer, list) -> list.add("TagPath")));
		addParameter(new Parameter("<value>", (explorer, list) -> list.add("TagValue")));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 4)
			return CommandResult.THROW_USAGE;

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return CommandResult.FAILURE;
		}

		try {
			Player player = (Player) sender;
			player.getInventory().setItemInMainHand(MMOLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand())
					.addTag(new ItemTag(args[2].toUpperCase().replace("-", "_"), args[3].replace("%%", " "))).toItem());
			player.sendMessage("Successfully set tag.");
			return CommandResult.SUCCESS;

		} catch (Exception exception) {
			sender.sendMessage("Couldn't set tag.");
			return CommandResult.FAILURE;
		}
	}
}
