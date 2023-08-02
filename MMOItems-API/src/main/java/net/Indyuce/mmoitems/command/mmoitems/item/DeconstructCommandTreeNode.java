package net.Indyuce.mmoitems.command.mmoitems.item;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class DeconstructCommandTreeNode extends CommandTreeNode {
	public DeconstructCommandTreeNode(CommandTreeNode parent) {
		super(parent, "deconstruct");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "该命令仅适用于玩家");
			return CommandResult.FAILURE;
		}

		Player player = (Player) sender;
		ItemStack stack = player.getInventory().getItemInMainHand();
		NBTItem item = MythicLib.plugin.getVersion().getWrapper().getNBTItem(stack);
		String tag = item.getString("MMOITEMS_TIER");
		if (tag.equals("")) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + "您持有的物品没有等级");
			return CommandResult.FAILURE;
		}

		ItemTier tier = MMOItems.plugin.getTiers().get(tag);
		if (tier == null) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + "该物品等级不存在");
			return CommandResult.FAILURE;
		}

		PlayerData data = PlayerData.get(player);
		List<ItemStack> loot = tier.getDeconstructedLoot(data);
		if (loot.isEmpty()) {
			sender.sendMessage(
					MMOItems.plugin.getPrefix() + "没有从分解中产生任何物品");
			return CommandResult.FAILURE;
		}

		stack.setAmount(stack.getAmount() - 1);
		player.getInventory().setItemInMainHand(stack);
		for (ItemStack drop : player.getInventory().addItem(loot.toArray(new ItemStack[0])).values())
			player.getWorld().dropItem(player.getLocation(), drop);

		sender.sendMessage(MMOItems.plugin.getPrefix() + "成功分解了你持有的物品");
		return CommandResult.SUCCESS;
	}
}
