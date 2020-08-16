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
		NBTItem item = MMOLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
		player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------------");
		player.sendMessage(ChatColor.AQUA + "Boolean = " + ChatColor.RESET + item.getBoolean("MMOITEMS_" + args[2].toUpperCase().replace("-", "_")));
		player.sendMessage(ChatColor.AQUA + "Double = " + ChatColor.RESET + item.getDouble("MMOITEMS_" + args[2].toUpperCase().replace("-", "_")));
		player.sendMessage(ChatColor.AQUA + "String = " + ChatColor.RESET + item.getString("MMOITEMS_" + args[2].toUpperCase().replace("-", "_")));
		return CommandResult.SUCCESS;
	}
}
