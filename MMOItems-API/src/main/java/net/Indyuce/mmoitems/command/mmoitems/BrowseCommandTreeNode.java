package net.Indyuce.mmoitems.command.mmoitems;

import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.gui.ItemBrowser;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class BrowseCommandTreeNode extends CommandTreeNode {
	public BrowseCommandTreeNode(CommandTreeNode parent) {
		super(parent, "browse");

		addParameter(MMOItemsCommandTreeRoot.TYPE);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "此命令仅适用于玩家");
			return CommandResult.FAILURE;
		}

		if (args.length < 2) {
			new ItemBrowser((Player) sender).open();
			return CommandResult.SUCCESS;
		}

		if (!Type.isValid(args[1])) {
			sender.sendMessage(MMOItems.plugin.getPrefix() + ChatColor.RED + "请指定有效的物品类型");
			return CommandResult.FAILURE;
		}

		new ItemBrowser((Player) sender, Type.get(args[1])).open();
		return CommandResult.SUCCESS;
	}
}
