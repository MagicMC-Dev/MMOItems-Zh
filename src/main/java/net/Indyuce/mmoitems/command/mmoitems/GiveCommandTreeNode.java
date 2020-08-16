package net.Indyuce.mmoitems.command.mmoitems;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.droptable.item.MMOItemDropItem;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.RandomAmount;
import net.Indyuce.mmoitems.api.util.message.Message;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import net.mmogroup.mmolib.api.util.SmartGive;
import net.mmogroup.mmolib.command.api.CommandTreeNode;
import net.mmogroup.mmolib.command.api.Parameter;

public class GiveCommandTreeNode extends CommandTreeNode {
	public GiveCommandTreeNode(CommandTreeNode parent) {
		super(parent, "give");

		addParameter(MMOItemsCommandTreeRoot.TYPE);
		addParameter(MMOItemsCommandTreeRoot.ID_2);
		addParameter(Parameter.PLAYER_OPTIONAL);
		addParameter(new Parameter("(min-max)", (explore, list) -> list.add("1-3")));
		addParameter(new Parameter("(unidentify-chance)", (explore, list) -> list.add("0")));
		addParameter(new Parameter("(drop-chance)", (explore, list) -> list.add("1")));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		try {
			Validate.isTrue(args.length > 3 || sender instanceof Player, "Please specify a player.");

			// target
			Player target = args.length > 3 ? Bukkit.getPlayer(args[3]) : (Player) sender;
			Validate.notNull(target, "Could not find player called '" + args[args.length > 3 ? 3 : 2] + "'.");

			// item
			Type type = MMOItems.plugin.getTypes().getOrThrow(args[1].toUpperCase().replace("-", "_"));
			MMOItemDropItem dropItem = new MMOItemDropItem(type, args[2].toUpperCase(), args.length > 6 ? Double.parseDouble(args[6]) / 100 : 1,
					args.length > 5 ? Double.parseDouble(args[5]) / 100 : 0, args.length > 4 ? new RandomAmount(args[4]) : new RandomAmount(1, 1));
			if (!dropItem.rollDrop())
				return CommandResult.SUCCESS;

			ItemStack item = dropItem.getItem(PlayerData.get(target));
			Validate.isTrue(item != null && item.getType() != Material.AIR, "Couldn't find/generate the item called '" + args[2].toUpperCase()
					+ "'. Check your console for potential item generation issues.");

			// message
			if (!sender.equals(target))
				sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.YELLOW + "Successfully gave " + ChatColor.GOLD
						+ MMOUtils.getDisplayName(item) + (item.getAmount() > 1 ? " x" + item.getAmount() : "") + ChatColor.YELLOW + " to "
						+ ChatColor.GOLD + target.getName() + ChatColor.YELLOW + ".");
			Message.RECEIVED_ITEM.format(ChatColor.YELLOW, "#item#", MMOUtils.getDisplayName(item), "#amount#",
					(item.getAmount() > 1 ? " x" + item.getAmount() : "")).send(target);

			// item
			new SmartGive(target).give(item);
			return CommandResult.SUCCESS;

		} catch (IllegalArgumentException exception) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + exception.getMessage());
			return CommandResult.FAILURE;
		}
	}
}
