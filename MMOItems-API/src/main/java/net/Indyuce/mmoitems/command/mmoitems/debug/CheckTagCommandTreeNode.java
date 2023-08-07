package net.Indyuce.mmoitems.command.mmoitems.debug;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

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
		NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
		String tag = item.hasTag(args[2]) ? args[2] : "MMOITEMS_" + args[2].toUpperCase().replace("-", "_");
		player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "--------------------------------------------------");
		player.sendMessage(ChatColor.AQUA + "Boolean = " + ChatColor.RESET + item.getBoolean(tag));
		player.sendMessage(ChatColor.AQUA + "Double = " + ChatColor.RESET + item.getDouble(tag));
		player.sendMessage(ChatColor.AQUA + "String = " + ChatColor.RESET + item.getString(tag));
		return CommandResult.SUCCESS;
	}
}
