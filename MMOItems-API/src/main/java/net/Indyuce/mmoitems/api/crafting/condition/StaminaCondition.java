package net.Indyuce.mmoitems.api.crafting.condition;

import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class StaminaCondition extends Condition {
	private final double amount;

	public StaminaCondition(MMOLineConfig config) {
		super("stamina");

		config.validate("amount");
		amount = config.getDouble("amount");
	}

	@Override
	public boolean isMet(PlayerData data) {
		return data.getRPG().getStamina() >= amount;
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#stamina#", "" + amount);
	}

	@Override
	public void whenCrafting(PlayerData data) {
		data.getRPG().giveStamina(-amount);
	}
}
