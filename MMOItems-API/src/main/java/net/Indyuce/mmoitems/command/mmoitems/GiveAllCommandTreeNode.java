package net.Indyuce.mmoitems.command.mmoitems;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.droptable.item.MMOItemDropItem;
import net.Indyuce.mmoitems.api.util.RandomAmount;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import io.lumine.mythic.lib.api.util.SmartGive;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;

public class GiveAllCommandTreeNode extends CommandTreeNode {
	public GiveAllCommandTreeNode(CommandTreeNode parent) {
		super(parent, "giveall");

		addParameter(MMOItemsCommandTreeRoot.TYPE);
		addParameter(MMOItemsCommandTreeRoot.ID_2);
		addParameter(new Parameter("<min-max>", (explore, list) -> list.add("1-3")));
		addParameter(new Parameter("<unidentify-chance>", (explore, list) -> list.add("<unidentify-chance>")));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		try {
			Validate.isTrue(args.length > 4, "Usage: /mi giveall <type> <item-id> <min-max> <unidentified-chance>");

			// item
			Type type = MMOItems.plugin.getTypes().getOrThrow(args[1]);
			ItemStack item = new MMOItemDropItem(type, args[2], 1, Double.parseDouble(args[4]) / 100, new RandomAmount(args[3])).getItem(null);
			Validate.isTrue(item != null && item.getType() != Material.AIR, "Couldn't find/generate the item called '" + args[1].toUpperCase()
					+ "'. Check your console for potential item generation issues.");

			for (Player target : Bukkit.getOnlinePlayers())
				new SmartGive(target).give(item);
			return CommandResult.SUCCESS;

		} catch (IllegalArgumentException exception) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + exception.getMessage());
			return CommandResult.FAILURE;
		}
	}
}
