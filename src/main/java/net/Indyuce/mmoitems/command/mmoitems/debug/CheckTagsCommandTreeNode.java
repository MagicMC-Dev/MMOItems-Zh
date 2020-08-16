package net.Indyuce.mmoitems.command.mmoitems.debug;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.command.api.CommandTreeNode;

public class CheckTagsCommandTreeNode extends CommandTreeNode {
	public CheckTagsCommandTreeNode(CommandTreeNode parent) {
		super(parent, "checktags");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return CommandResult.FAILURE;
		}

		Player player = (Player) sender;
		player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------------");
		for (String s : MMOLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand()).getTags())
			player.sendMessage("- " + s);
		return CommandResult.SUCCESS;
	}
}
