package net.Indyuce.mmoitems.command.completion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.command.PluginHelp;

public class MMOItemsCompletion implements TabCompleter {
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("mmoitems.admin"))
			return null;

		List<String> list = new ArrayList<>();

		if (args.length == 1) {
			list.add("edit");
			list.add("create");
			list.add("browse");
			list.add("load");
			list.add("copy");
			list.add("drop");
			list.add("itemlist");
			list.add("reload");
			list.add("list");
			list.add("help");
			list.add("delete");
			list.add("remove");
			list.add("heal");
			list.add("identify");
			list.add("unidentify");
			list.add("info");
			list.add("ability");
			list.add("allitems");
			list.add("update");
			list.add("stations");
			list.add("giveall");

		} else if (args.length == 2) {
			if (args[0].equalsIgnoreCase("help"))
				for (int j = 1; j <= PluginHelp.getMaxPage(); j++)
					list.add("" + j);

			else if (args[0].equalsIgnoreCase("ability"))
				list.addAll(MMOItems.plugin.getAbilities().getAbilityKeys());

			else if (args[0].equalsIgnoreCase("update")) {
				list.add("list");
				list.add("apply");
				list.add("info");

			} else if (args[0].equalsIgnoreCase("stations")) {
				list.add("list");
				list.add("open");

			} else if (args[0].equalsIgnoreCase("reload")) {
				list.add("adv-recipes");
				list.add("stations");

			} else if (args[0].equalsIgnoreCase("list")) {
				list.add("ability");
				list.add("type");
				list.add("spirit");
			}

			else if (args[0].equalsIgnoreCase("browse") || args[0].equalsIgnoreCase("itemlist") || args[0].equalsIgnoreCase("drop") || args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("copy") || args[0].equalsIgnoreCase("load") || args[0].equalsIgnoreCase("giveall"))
				for (Type type : MMOItems.plugin.getTypes().getAll())
					list.add(type.getId());

			else if (Type.isValid(args[0]))
				MMOItems.plugin.getTypes().get(args[0].toUpperCase().replace("-", "_")).getConfigFile().getConfig().getKeys(false).forEach(key -> list.add(key.toUpperCase()));

		} else if (args.length == 3) {
			if (args[0].equalsIgnoreCase("ability") || Type.isValid(args[0]))
				Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName()));

			else if (args[0].equalsIgnoreCase("update") && (args[1].equalsIgnoreCase("apply") || args[1].equalsIgnoreCase("info")))
				MMOItems.plugin.getUpdates().getAll().forEach(update -> list.add("" + update.getId()));

			else if (args[0].equalsIgnoreCase("stations") && args[1].equalsIgnoreCase("open"))
				MMOItems.plugin.getCrafting().getAll().forEach(station -> list.add(station.getId()));

			else if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("copy") || args[0].equalsIgnoreCase("drop") || args[0].equalsIgnoreCase("giveall"))
				if (Type.isValid(args[1]))
					Type.get(args[1]).getConfigFile().getConfig().getKeys(false).forEach(key -> list.add(key.toUpperCase()));

		} else if (args[0].equals("drop")) {
			if (args.length == 4)
				Bukkit.getWorlds().forEach(world -> list.add(world.getName()));

			if (args.length == 5)
				list.add("" + (sender instanceof Player ? (int) ((Player) sender).getLocation().getX() : 0));

			if (args.length == 6)
				list.add("" + (sender instanceof Player ? (int) ((Player) sender).getLocation().getY() : 0));

			if (args.length == 7)
				list.add("" + (sender instanceof Player ? (int) ((Player) sender).getLocation().getZ() : 0));

			if (args.length == 8 || args.length == 10)
				for (int j = 0; j <= 100; j += 10)
					list.add("" + j);

			if (args.length == 9)
				for (int j = 0; j < 4; j++)
					for (int k = j; k < 4; k++)
						list.add(j + "-" + k);

		} else if (args[0].equalsIgnoreCase("stations") && args[1].equalsIgnoreCase("open")) {
			Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName()));

		} else if (args[0].equalsIgnoreCase("ability")) {
			String path = args[1].toUpperCase().replace("-", "_");
			if (MMOItems.plugin.getAbilities().hasAbility(path)) {
				Ability ability = MMOItems.plugin.getAbilities().getAbility(path);
				if (Math.floorMod(args.length, 2) == 0)
					list.addAll(ability.getModifiers());
				else
					for (int j = 0; j < 10; j++)
						list.add("" + j);
			}
		} else if (args[0].equalsIgnoreCase("giveall")) {
			if (args.length == 4)
				for (String str : new String[] { "1", "16", "64", "1-5", "1-10", "4-16" })
					list.add(str);

			if (args.length == 5)
				for (int j : new int[] { 0, 10, 25, 50, 75, 100 })
					list.add("" + j);
		} else if (Type.isValid(args[0])) {
			if (args.length == 4)
				for (String str : new String[] { "1", "16", "64", "1-5", "1-10", "4-16" })
					list.add(str);

			if (args.length == 5 || args.length == 6)
				for (int j : new int[] { 0, 10, 25, 50, 75, 100 })
					list.add("" + j);
		}

		return args[args.length - 1].isEmpty() ? list : list.stream().filter(string -> string.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
	}
}
