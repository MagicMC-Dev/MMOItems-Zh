package net.Indyuce.mmoitems.api.util.message;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmocore.api.player.PlayerActivity;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PlayerMessage {
	private final boolean actionBar;

	private String message;

	/**
	 * Used to block empty messages from spamming the chat. Also used to apply
	 * a default color code to the message and be able to select which
	 * messages are displayed on the action bar and which go to chat.
	 *
	 * @param message Unformatted message
	 */
	public PlayerMessage(Message message) {
		this.message = message.getUpdated();
		this.actionBar = MMOItems.plugin.getConfig().getBoolean("action-bar-display." + message.getActionBarConfigPath());
	}

	/**
	 * Can be used by external plugins. This can be useful to
	 * apply that 60 tick action bar timeout when using MMOCore
	 *
	 * @param message   Messages with color codes applied.
	 * @param actionBar Should the message be displayed on the action bar
	 */
	public PlayerMessage(String message, boolean actionBar) {
		this.message = message;
		this.actionBar = actionBar;
	}

	public PlayerMessage format(ChatColor prefix, String... toReplace) {
		message = prefix + message;
		for (int j = 0; j < toReplace.length; j += 2)
			message = message.replace(toReplace[j], toReplace[j + 1]);
		return this;
	}

	/**
	 * Either sends to action bar or to chat if it's not empty
	 *
	 * @param player Player to send message to
	 */
	public void send(Player player) {
		if (ChatColor.stripColor(message).isEmpty())
			return;

		if (actionBar) {
			if (Bukkit.getPluginManager().isPluginEnabled("MMOCore"))
				PlayerData.get(player).setLastActivity(PlayerActivity.ACTION_BAR_MESSAGE);

			MythicLib.plugin.getVersion().getWrapper().sendActionBar(player, message);
		} else
			player.sendMessage(message);
	}

	@Override
	public String toString() {
		return message;
	}
}
