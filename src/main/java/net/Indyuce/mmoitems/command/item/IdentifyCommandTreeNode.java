package net.Indyuce.mmoitems.command.item;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.util.identify.IdentifiedItem;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.command.api.CommandTreeNode;

public class IdentifyCommandTreeNode extends CommandTreeNode {
	public IdentifyCommandTreeNode(CommandTreeNode parent) {
		super(parent, "identify");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return CommandResult.FAILURE;
		}

		Player player = (Player) sender;
		NBTItem item = MMOLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
		String tag = item.getString("MMOITEMS_UNIDENTIFIED_ITEM");
		if (tag.equals("")) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + "The item you are holding is already identified.");
			return CommandResult.FAILURE;
		}

		final int amount = player.getInventory().getItemInMainHand().getAmount();
		ItemStack identifiedItem = new IdentifiedItem(item).identify();
		identifiedItem.setAmount(amount);

		player.getInventory().setItemInMainHand(identifiedItem);
		sender.sendMessage(MMOItems.plugin.getPrefix() + "Successfully identified the item you are holding.");
		return CommandResult.SUCCESS;
	}
}
