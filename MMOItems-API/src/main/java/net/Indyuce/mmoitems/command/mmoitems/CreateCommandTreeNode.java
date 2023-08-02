package net.Indyuce.mmoitems.command.mmoitems;

import io.lumine.mythic.lib.command.api.CommandTreeNode;
import io.lumine.mythic.lib.command.api.Parameter;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import net.Indyuce.mmoitems.gui.edition.ItemEdition;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateCommandTreeNode extends CommandTreeNode {
	public CreateCommandTreeNode(CommandTreeNode parent) {
		super(parent, "create");

		addParameter(MMOItemsCommandTreeRoot.TYPE);
		addParameter(new Parameter("<id>", (explorer, list) -> list.add("ITEM_ID")));
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 3)
			return CommandResult.THROW_USAGE;

		if (!Type.isValid(args[1])) {
			sender.sendMessage(
					MMOItems.plugin.getPrefix() + ChatColor.RED + "没有名为 " + args[1].toUpperCase().replace("-", "_") + "的物品类型");
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "输入 " + ChatColor.GREEN + "/mi list type" + ChatColor.RED
					+ " 查看所有可用的物品类型");
			return CommandResult.FAILURE;
		}

		Type type = Type.get(args[1]);
		String name = args[2].toUpperCase().replace("-", "_");
		ConfigFile config = type.getConfigFile();
		if (config.getConfig().contains(name)) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "已经有一个物品叫做 " + name + "。");
			return CommandResult.FAILURE;
		}

		config.getConfig().set(name + ".base.material", type.getItem().getType().name());
		config.save();
		MMOItems.plugin.getTemplates().requestTemplateUpdate(type, name);

		if (sender instanceof Player)
			new ItemEdition((Player) sender, MMOItems.plugin.getTemplates().getTemplate(type, name)).open();
		sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.GREEN + "您已成功创建 " + name + "。");
		return CommandResult.SUCCESS;
	}
}
