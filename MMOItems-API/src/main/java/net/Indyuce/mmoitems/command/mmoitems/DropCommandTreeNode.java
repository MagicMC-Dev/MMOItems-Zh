package net.Indyuce.mmoitems.command.mmoitems;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.droptable.item.MMOItemDropItem;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;

public class DropCommandTreeNode extends CommandTreeNode {
	public DropCommandTreeNode(CommandTreeNode parent) {
		super(parent, "drop");

		addParameter(MMOItemsCommandTreeRoot.TYPE);
		addParameter(MMOItemsCommandTreeRoot.ID_2);
		addParameter(new Parameter("<world>", (explore, list) -> Bukkit.getWorlds().forEach(world -> list.add(world.getName()))));
		addParameter(new Parameter("<x>", (explore, list) -> list.add("<x>")));
		addParameter(new Parameter("<y>", (explore, list) -> list.add("<y>")));
		addParameter(new Parameter("<z>", (explore, list) -> list.add("<z>")));
		addParameter(new Parameter("<drop-chance>", (explore, list) -> list.add("<drop-chance>")));
		addParameter(new Parameter("<min-max>", (explore, list) -> list.add("1-3")));
		addParameter(new Parameter("<unidentify-chance>", (explore, list) -> list.add("<unidentify-chance>")));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length != 10)
			return CommandResult.THROW_USAGE;

		if (!Type.isValid(args[1])) {
			sender.sendMessage(
					MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[1].toUpperCase().replace("-", "_") + ".");
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Type " + ChatColor.GREEN + "/mi list type " + ChatColor.RED
					+ "to see all the available item types.");
			return CommandResult.FAILURE;
		}

		Type type = Type.get(args[1].toUpperCase());
		String name = args[2].toUpperCase().replace("-", "_");
		FileConfiguration config = type.getConfigFile().getConfig();
		if (!config.contains(name)) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item called " + name + ".");
			return CommandResult.FAILURE;
		}

		World world = Bukkit.getWorld(args[3]);
		if (world == null) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Couldn't find the world called " + args[3] + ".");
			return CommandResult.FAILURE;
		}

		double x, y, z, dropChance, unidentifiedChance;
		int min, max;

		try {
			x = Double.parseDouble(args[4]);
		} catch (Exception e) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + args[4] + " is not a valid number.");
			return CommandResult.FAILURE;
		}

		try {
			y = Double.parseDouble(args[5]);
		} catch (Exception e) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + args[5] + " is not a valid number.");
			return CommandResult.FAILURE;
		}

		try {
			z = Double.parseDouble(args[6]);
		} catch (Exception e) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + args[6] + " is not a valid number.");
			return CommandResult.FAILURE;
		}

		try {
			dropChance = Double.parseDouble(args[7]);
		} catch (Exception e) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + args[7] + " is not a valid number.");
			return CommandResult.FAILURE;
		}

		try {
			unidentifiedChance = Double.parseDouble(args[9]);
		} catch (Exception e) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + args[9] + " is not a valid number.");
			return CommandResult.FAILURE;
		}

		String[] splitAmount = args[8].split("-");
		if (splitAmount.length != 2) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "The drop quantity format is incorrect.");
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "Format: [min]-[max]");
			return CommandResult.FAILURE;
		}

		try {
			min = Integer.parseInt(splitAmount[0]);
		} catch (Exception e) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + splitAmount[0] + " is not a valid number.");
			return CommandResult.FAILURE;
		}

		try {
			max = Integer.parseInt(splitAmount[1]);
		} catch (Exception e) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + splitAmount[1] + " is not a valid number.");
			return CommandResult.FAILURE;
		}

		ItemStack item = new MMOItemDropItem(type, name, dropChance / 100, unidentifiedChance / 100, min, max).getItem(null);
		if (item == null || item.getType() == Material.AIR) {
			sender.sendMessage(
					MMOItems.plugin.getPrefix() + ChatColor.RED + "An error occurred while attempting to generate the item called " + name + ".");
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "See console for more information!");
			return CommandResult.FAILURE;
		}

		world.dropItem(new Location(world, x, y, z), item);
		return CommandResult.SUCCESS;
	}
}
