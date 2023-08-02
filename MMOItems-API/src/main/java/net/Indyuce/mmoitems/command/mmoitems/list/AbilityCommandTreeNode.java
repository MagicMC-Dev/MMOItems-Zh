package net.Indyuce.mmoitems.command.mmoitems.list;

import net.Indyuce.mmoitems.skill.RegisteredSkill;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import net.Indyuce.mmoitems.MMOItems;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class AbilityCommandTreeNode extends CommandTreeNode {
	public AbilityCommandTreeNode(CommandTreeNode parent) {
		super(parent, "ability");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " 能力 "
				+ ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "]-----------------");
		sender.sendMessage(ChatColor.WHITE + "以下是您可以绑定到物品的所有能力");
		sender.sendMessage(ChatColor.WHITE + "括号内的值为 " + ChatColor.UNDERLINE + " 修改器 " + ChatColor.WHITE
				+ " 它允许你改变能力值 (冷却时间、伤害…… ) ");
		for (RegisteredSkill ability : MMOItems.plugin.getSkills().getAll()) {
			String modFormat = ChatColor.GRAY + String.join(ChatColor.WHITE + ", " + ChatColor.GRAY, ability.getHandler().getModifiers());
			modFormat = ChatColor.WHITE + "(" + modFormat + ChatColor.WHITE + ")";
			sender.sendMessage("* " + ChatColor.LIGHT_PURPLE + ability.getName() + " " + modFormat);
		}
		return CommandResult.SUCCESS;
	}
}
