package net.Indyuce.mmoitems.command.mmoitems;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.command.MMOItemsCommandTreeRoot;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.command.api.CommandTreeNode;

public class ItemListCommandTreeNode extends CommandTreeNode {
	public ItemListCommandTreeNode(CommandTreeNode parent) {
		super(parent, "itemlist");

		addParameter(MMOItemsCommandTreeRoot.TYPE);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if (args.length < 2)
			return CommandResult.THROW_USAGE;

		if (!Type.isValid(args[1])) {
			sender.sendMessage(
					MMOItems.plugin.getPrefix() + ChatColor.RED + "There is no item type called " + args[1].toUpperCase().replace("-", "_"));
			sender.sendMessage(MMOItems.plugin.getPrefix() + "Type " + ChatColor.GREEN + "/mi list type " + ChatColor.GRAY
					+ "to see all the available item types.");
			return CommandResult.FAILURE;
		}

		Type type = Type.get(args[1]);
		sender.sendMessage(ChatColor.YELLOW + "" + ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
		sender.sendMessage(ChatColor.GREEN + "List of all items in " + type.getId().toLowerCase() + ".yml:");
		FileConfiguration config = type.getConfigFile().getConfig();

		if (sender instanceof Player)
			for (String s : config.getKeys(false)) {
				String nameFormat = config.getConfigurationSection(s).contains("name")
						? " " + ChatColor.WHITE + "(" + MythicLib.plugin.parseColors(config.getString(s + ".name")) + ChatColor.WHITE + ")"
						: "";
				MythicLib.plugin.getVersion().getWrapper().sendJson((Player) sender,
						"{\"text\":\"* " + ChatColor.GREEN + s + nameFormat + "\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/mi edit "
								+ type.getId() + " " + s + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"Click to edit "
								+ (nameFormat.equals("") ? s : MythicLib.plugin.parseColors(config.getString(s + ".name"))) + ChatColor.WHITE
								+ ".\",\"color\":\"white\"}}}");
			}

		else
			for (String s : config.getKeys(false))
				sender.sendMessage("* " + ChatColor.GREEN + s
						+ (config.getConfigurationSection(s).contains("name")
								? " " + ChatColor.WHITE + "(" + MythicLib.plugin.parseColors(config.getString(s + ".name")) + ChatColor.WHITE + ")"
								: ""));

		return CommandResult.SUCCESS;
	}
}
