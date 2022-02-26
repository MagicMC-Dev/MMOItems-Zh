package net.Indyuce.mmoitems.command.mmoitems.item;

import net.Indyuce.mmoitems.api.Type;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class UnidentifyCommandTreeNode extends CommandTreeNode {
	public UnidentifyCommandTreeNode(CommandTreeNode parent) {
		super(parent, "unidentify");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return CommandResult.FAILURE;
		}

		Player player = (Player) sender;
		NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(player.getInventory().getItemInMainHand());
		if (item.getType() == null) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + "Couldn't unidentify the item you are holding.");
			return CommandResult.FAILURE;
		}

		if (item.getBoolean("MMOITEMS_UNIDENTIFIED")) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + "The item you are holding is already unidentified.");
			return CommandResult.FAILURE;
		}

		player.getInventory().setItemInMainHand(Type.get(item.getType()).getUnidentifiedTemplate().newBuilder(item).build());
		sender.sendMessage(MMOItems.plugin.getPrefix() + "Successfully unidentified the item you are holding.");
		return CommandResult.SUCCESS;
	}
}
