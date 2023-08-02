package net.Indyuce.mmoitems.command.mmoitems.update;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.PluginUpdate;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;


public class ApplyCommandTreeNode extends CommandTreeNode {
	public ApplyCommandTreeNode(CommandTreeNode parent) {
		super(parent, "apply");

		addParameter(
				new Parameter("<id>", (explorer, list) -> MMOItems.plugin.getUpdates().getAll().forEach(update -> list.add("" + update.getId()))));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		int id;
		try {
			id = Integer.parseInt(args[2]);
		} catch (NumberFormatException exception) {
			sender.sendMessage(ChatColor.RED + "请指定一个有效的数字");
			return CommandResult.FAILURE;
		}

		if (!MMOItems.plugin.getUpdates().has(id)) {
			sender.sendMessage(ChatColor.RED + "找不到任何带 ID 的配置更新 " + id);
			return CommandResult.FAILURE;
		}

		PluginUpdate update = MMOItems.plugin.getUpdates().get(id);
		sender.sendMessage(ChatColor.YELLOW + "应用配置更新 " + id + "...");
		update.apply(sender);
		sender.sendMessage(
				ChatColor.YELLOW + "配置 " + id + " 更新已成功应用 检查控制台是否有潜在的更新错误日志");
		return CommandResult.SUCCESS;
	}
}
