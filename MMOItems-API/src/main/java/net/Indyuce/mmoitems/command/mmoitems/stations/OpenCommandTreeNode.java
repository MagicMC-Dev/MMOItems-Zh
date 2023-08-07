package net.Indyuce.mmoitems.command.mmoitems.stations;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.gui.CraftingStationView;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;

public class OpenCommandTreeNode extends CommandTreeNode {
	public OpenCommandTreeNode(CommandTreeNode parent) {
		super(parent, "open");

		addParameter(new Parameter("<station>",
				(explorer, list) -> MMOItems.plugin.getCrafting().getStations().forEach(station -> list.add(station.getId()))));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		if (!MMOItems.plugin.getCrafting().hasStation(args[2])) {
			sender.sendMessage(ChatColor.RED + "There is no station called " + args[2] + ".");
			return CommandResult.FAILURE;
		}

		Player target = args.length > 3 ? Bukkit.getPlayer(args[3]) : (sender instanceof Player ? (Player) sender : null);
		if (target == null) {
			sender.sendMessage(ChatColor.RED + "Please specify a valid player.");
			return CommandResult.FAILURE;
		}

		new CraftingStationView(target, MMOItems.plugin.getCrafting().getStation(args[2]), 1).open();
		return CommandResult.SUCCESS;
	}
}
