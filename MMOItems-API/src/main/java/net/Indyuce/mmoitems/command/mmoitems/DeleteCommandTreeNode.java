package net.Indyuce.mmoitems.command.mmoitems;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class DeleteCommandTreeNode extends CommandTreeNode {
	public DeleteCommandTreeNode(CommandTreeNode parent) {
		super(parent, "delete");

		addParameter(MMOItemsCommandTreeRoot.TYPE);
		addParameter(MMOItemsCommandTreeRoot.ID_2);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		if (!Type.isValid(args[1])) {
			sender.sendMessage(
					MMOItems.plugin.getPrefix() + ChatColor.RED + "没有名为 " + args[1].toUpperCase().replace("-", "_") + " 的物品类型");
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "输入 " + ChatColor.GREEN + "/mi list type" + ChatColor.RED
					+ " 查看所有可用的物品类型");
			return CommandResult.FAILURE;
		}

		Type type = Type.get(args[1]);
		String id = args[2].toUpperCase().replace("-", "_");
		if (!MMOItems.plugin.getTemplates().hasTemplate(type, id)) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "没有名为 " + id + "的物品");
			return CommandResult.FAILURE;
		}

		MMOItems.plugin.getTemplates().deleteTemplate(type, id);
		sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GREEN + "您已成功删除 " + id );
		return CommandResult.SUCCESS;
	}
}
