package net.Indyuce.mmoitems.api.crafting.trigger;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class MessageTrigger extends Trigger {
	private final String message;

	public MessageTrigger(MMOLineConfig config) {
		super("message");

		config.validate("format");
		message = config.getString("format");
	}

	@Override
	public void whenCrafting(PlayerData data) {
		if(!data.isOnline()) return;
		data.getPlayer().sendMessage(MythicLib.plugin.getPlaceholderParser().parse(data.getPlayer(), message));
	}
}
