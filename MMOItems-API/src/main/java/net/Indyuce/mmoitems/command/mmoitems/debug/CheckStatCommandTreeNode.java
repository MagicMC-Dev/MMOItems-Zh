package net.Indyuce.mmoitems.command.mmoitems.debug;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class CheckStatCommandTreeNode extends CommandTreeNode {
	public CheckStatCommandTreeNode(CommandTreeNode parent) {
		super(parent, "checkstat");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return CommandResult.FAILURE;
		}

		ItemStat stat = MMOItems.plugin.getStats().get(args[2].toUpperCase().replace("-", "_"));
		if (stat == null) {
			sender.sendMessage(ChatColor.RED + "Couldn't find the stat called " + args[2].toUpperCase().replace("-", "_") + ".");
			return CommandResult.FAILURE;
		}

		Player player = (Player) sender;
		player.sendMessage("Found stat with ID " + stat.getId() + " = " + PlayerData.get((Player) sender).getStats().getStat(stat));
		return CommandResult.SUCCESS;
	}
}
