package net.Indyuce.mmoitems.api.crafting.trigger;

import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandTrigger extends Trigger {
	private String command;
	private final String sender;

	public CommandTrigger(MMOLineConfig config) {
		super("command");

		config.validate("format");
		sender = config.getString("sender", "PLAYER").toUpperCase();
		command = config.getString("format");
	}

	@Override
	public void whenCrafting(PlayerData data) {
		if(!data.isOnline()) return;
		dispatchCommand(data.getPlayer(), sender.equals("CONSOLE"), sender.equals("OP"));
	}
	
	private void dispatchCommand(Player player, boolean console, boolean op) {

		// Adds back using "%player%" in the command trigger string.
		command = command.replaceAll("(?i)%player%", player.getName());

		if (console) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
			return;
		}

		if (op && !player.isOp()) {
			player.setOp(true);
			try {
				Bukkit.dispatchCommand(player, command);
			} catch (Exception ignored) {}
			player.setOp(false);
		} else
			Bukkit.dispatchCommand(player, command);
	}
}
