package net.Indyuce.mmoitems.comp.mmocore.crafting;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmoitems.api.crafting.condition.Condition;
import net.Indyuce.mmoitems.api.player.PlayerData;

public class StelliumCondition extends Condition {
	private final double amount;

	public StelliumCondition(MMOLineConfig config) {
		super("mana");

		config.validate("amount");
		amount = config.getDouble("amount");
	}

	@Override
	public boolean isMet(PlayerData data) {
		return net.Indyuce.mmocore.api.player.PlayerData.get(data.getUniqueId()).getStellium() >= amount;
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#stellium#", "" + amount);
	}

	@Override
	public void whenCrafting(PlayerData data) {
		net.Indyuce.mmocore.api.player.PlayerData.get(data.getUniqueId()).giveStellium(-amount);
	}
}
