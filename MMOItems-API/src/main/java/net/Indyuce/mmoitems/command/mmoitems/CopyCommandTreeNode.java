package net.Indyuce.mmoitems.command.mmoitems;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;

public class CopyCommandTreeNode extends CommandTreeNode {
	public CopyCommandTreeNode(CommandTreeNode parent) {
		super(parent, "copy");

		addParameter(MMOItemsCommandTreeRoot.TYPE);
		addParameter(MMOItemsCommandTreeRoot.ID_2);
		addParameter(new Parameter("<new-id>", (a, b) -> {
		}));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 4)
			return CommandResult.THROW_USAGE;

		if (!Type.isValid(args[1])) {
			sender.sendMessage(
					MMOItems.plugin.getPrefix() + ChatColor.RED + "没有名为的" + args[1].toUpperCase().replace("-", "_") + " 的物品");
			sender.sendMessage(MMOItems.plugin.getPrefix() + "输入" + ChatColor.GREEN + "/mi list type " + ChatColor.GRAY
					+ " 查看所有可用的物品类型");
			return CommandResult.FAILURE;
		}

		Type type = Type.get(args[1]);
		ConfigFile config = type.getConfigFile();
		String id1 = args[2].toUpperCase().replace("-", "_");
		if (!config.getConfig().contains(id1)) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "没有名为 " + id1 + " 的物品");
			return CommandResult.FAILURE;
		}

		String id2 = args[3].toUpperCase();
		if (config.getConfig().contains(id2)) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "已经有一个物品叫做 " + id2 + "!");
			return CommandResult.FAILURE;
		}

		config.getConfig().set(id2, config.getConfig().getConfigurationSection(id1));
		config.save();
		MMOItems.plugin.getTemplates().requestTemplateUpdate(type, id2);

		if (sender instanceof Player)
			new ItemEdition((Player) sender, MMOItems.plugin.getTemplates().getTemplate(type, id2)).open();
		sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GREEN + "已成功复制" + id1 + " 至  " + id2 + "!");
		return CommandResult.SUCCESS;
	}
}
