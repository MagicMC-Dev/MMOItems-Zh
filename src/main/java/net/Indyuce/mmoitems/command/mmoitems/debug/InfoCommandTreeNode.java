package net.Indyuce.mmoitems.command.mmoitems.debug;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import io.lumine.mythic.lib.mmolibcommands.api.CommandTreeNode;
import io.lumine.mythic.lib.mmolibcommands.api.Parameter;

import java.util.Arrays;

public class InfoCommandTreeNode extends CommandTreeNode {
	public InfoCommandTreeNode(CommandTreeNode parent) {
		super(parent, "info");

		addParameter(Parameter.PLAYER_OPTIONAL);
	}

	@Override
	public CommandResult execute(CommandSender sender, String[] args) {
		if(Arrays.asList(args).contains("showidentity")) {
			sender.sendMessage(String.format("Tu identidad es %s", MMOItems.plugin.getLanguage().elDescargadorLaIdentidad));
			return CommandResult.SUCCESS;
		}
		Player player = args.length > 2 ? Bukkit.getPlayer(args[2]) : (sender instanceof Player ? (Player) sender : null);
		if (player == null) {
			sender.sendMessage(ChatColor.RED + "Couldn't find target player.");
			return CommandResult.FAILURE;
		}

		RPGPlayer rpg = PlayerData.get(player).getRPG();
		sender.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "-----------------[" + ChatColor.LIGHT_PURPLE + " Player Information "
				+ ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "]-----------------");
		sender.sendMessage(ChatColor.WHITE + "Information about " + ChatColor.LIGHT_PURPLE + player.getName());
		sender.sendMessage("");
		sender.sendMessage(ChatColor.WHITE + "Player Class: " + ChatColor.LIGHT_PURPLE + rpg.getClassName());
		sender.sendMessage(ChatColor.WHITE + "Player Level: " + ChatColor.LIGHT_PURPLE + rpg.getLevel());
		sender.sendMessage(ChatColor.WHITE + "Player Mana: " + ChatColor.LIGHT_PURPLE + rpg.getMana());
		sender.sendMessage(ChatColor.WHITE + "Player Stamina: " + ChatColor.LIGHT_PURPLE + rpg.getStamina());
		return CommandResult.SUCCESS;
	}
}
