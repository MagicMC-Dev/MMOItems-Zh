package net.Indyuce.mmoitems.api.util.message;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmoitems.MMOItems;
import net.mmogroup.mmolib.MMOLib;

public class PlayerMessage {
	private String message;

	/*
	 * this class allows the plugin to block empty messages from sending to
	 * player chat
	 */
	public PlayerMessage(String message) {
		this.message = message;
	}

	public PlayerMessage format(ChatColor prefix, String... toReplace) {
		message = prefix + message;
		for (int j = 0; j < toReplace.length; j += 2)
			message = message.replace(toReplace[j], toReplace[j + 1]);
		return this;
	}

	public void send(CommandSender player) {
		if (!ChatColor.stripColor(message).equals(""))
			player.sendMessage(message);
	}

	// send on action bar or chat
	public void send(Player player, String actionBarBooleanPath) {
		if (ChatColor.stripColor(message).equals(""))
			return;

		if (MMOItems.plugin.getConfig().getBoolean("action-bar-display." + actionBarBooleanPath)) {
			if (Bukkit.getPluginManager().isPluginEnabled("MMOCore"))
				PlayerData.get(player).setActionBarTimeOut(60);

			MMOLib.plugin.getVersion().getWrapper().sendActionBar(player, message);
		} else
			player.sendMessage(message);
	}

	@Override
	public String toString() {
		return message;
	}
}
