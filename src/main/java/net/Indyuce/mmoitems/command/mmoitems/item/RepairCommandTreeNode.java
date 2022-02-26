package net.Indyuce.mmoitems.command.mmoitems.item;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.interaction.util.DurabilityItem;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class RepairCommandTreeNode extends CommandTreeNode {
	public RepairCommandTreeNode(CommandTreeNode parent) {
		super(parent, "repair");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "This command is only for players.");
			return CommandResult.FAILURE;
		}

		Player player = (Player) sender;
		ItemStack stack = player.getInventory().getItemInMainHand();
		NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(stack);

		if (!item.hasTag("MMOITEMS_DURABILITY")) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + "The item you are holding can't be repaired.");
			return CommandResult.FAILURE;
		}

		DurabilityItem durItem = new DurabilityItem(player, stack);
		player.getInventory().setItemInMainHand(durItem.addDurability(durItem.getMaxDurability()).toItem());

		sender.sendMessage(MMOItems.plugin.getPrefix() + "Successfully repaired the item you are holding.");
		return CommandResult.SUCCESS;
	}
}
