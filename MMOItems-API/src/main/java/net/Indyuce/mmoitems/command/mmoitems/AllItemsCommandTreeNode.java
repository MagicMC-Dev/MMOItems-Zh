package net.Indyuce.mmoitems.command.mmoitems;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class AllItemsCommandTreeNode extends CommandTreeNode {
	public AllItemsCommandTreeNode(CommandTreeNode parent) {
		super(parent, "allitems");
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
		sender.sendMessage(ChatColor.GREEN + "List of all MMOItems:");
		for (Type type : MMOItems.plugin.getTypes().getAll()) {
			FileConfiguration config = type.getConfigFile().getConfig();
			for (String s : config.getKeys(false))
				sender.sendMessage("* " + ChatColor.GREEN + s
						+ (config.getConfigurationSection(s).contains("name")
								? " " + ChatColor.WHITE + "(" + MythicLib.plugin.parseColors(config.getString(s + ".name")) + ChatColor.WHITE + ")"
								: ""));
		}
		return CommandResult.SUCCESS;
	}
}
