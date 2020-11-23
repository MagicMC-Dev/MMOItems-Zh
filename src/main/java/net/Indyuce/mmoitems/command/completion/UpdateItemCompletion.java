package net.Indyuce.mmoitems.command.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;

public class UpdateItemCompletion implements TabCompleter {
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("mmoitems.update"))
			return null;

		List<String> list = new ArrayList<>();

		if (args.length == 1)
			for (Type type : MMOItems.plugin.getTypes().getAll())
				list.add(type.getId());

		if (args.length == 2 && Type.isValid(args[0]))
			Type.get(args[0]).getConfigFile().getConfig().getKeys(false).forEach(id -> list.add(id.toUpperCase()));

		return args[args.length - 1].isEmpty() ? list : list.stream().filter(string -> string.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
	}
}
