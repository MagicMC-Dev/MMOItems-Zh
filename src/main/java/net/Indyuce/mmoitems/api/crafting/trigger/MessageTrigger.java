package net.Indyuce.mmoitems.api.crafting.trigger;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class MessageTrigger extends Trigger {
	private final String message;

	public MessageTrigger(MMOLineConfig config) {
		super("message");

		config.validate("format");
		message = config.getString("format");
	}

	@Override
	public void whenCrafting(PlayerData data) {
		data.getPlayer().sendMessage(MMOItems.plugin.getPlaceholderParser().parse(data.getPlayer(), message));
	}
}
