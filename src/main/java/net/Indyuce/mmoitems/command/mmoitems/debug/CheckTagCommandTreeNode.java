package net.Indyuce.mmoitems.command.mmoitems.debug;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.command.api.CommandTreeNode;

public class CheckTagCommandTreeNode extends CommandTreeNode {
	public CheckTagCommandTreeNode(CommandTreeNode parent) {
		super(parent, "checktag");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return CommandResult.FAILURE;
		}

		Player player = (Player) sender;
		String tag = args[2].contains("#") ? args[2].substring(1) : "MMOITEMS_" + args[2];
		NBTItem item = MMOLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
		player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------------");
		player.sendMessage(ChatColor.AQUA + "Boolean = " + ChatColor.RESET + item.getBoolean(tag.toUpperCase().replace("-", "_")));
		player.sendMessage(ChatColor.AQUA + "Double = " + ChatColor.RESET + item.getDouble(tag.toUpperCase().replace("-", "_")));
		player.sendMessage(ChatColor.AQUA + "String = " + ChatColor.RESET + item.getString(tag.toUpperCase().replace("-", "_")));
		return CommandResult.SUCCESS;
	}
}
