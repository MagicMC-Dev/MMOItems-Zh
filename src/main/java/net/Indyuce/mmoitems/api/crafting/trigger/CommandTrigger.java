package net.Indyuce.mmoitems.api.crafting.trigger;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.mmogroup.mmolib.api.MMOLineConfig;
import org.bukkit.Bukkit;

public class CommandTrigger extends Trigger {
	private final String command;
	private final boolean player;

	public CommandTrigger(MMOLineConfig config) {
		super("command");

		config.validate("format");
		player = config.getBoolean("player", false);
		command = config.getString("format");
	}

	@Override
	public void whenCrafting(PlayerData data) {
		Bukkit.dispatchCommand(player ? data.getPlayer() : Bukkit.getConsoleSender(),
				command.replace("%player%", data.getPlayer().getName()));
	}
}
