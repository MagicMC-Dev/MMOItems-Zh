package net.Indyuce.mmoitems.api.crafting.condition;

import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class ManaCondition extends Condition {
	private final double amount;

	public ManaCondition(MMOLineConfig config) {
		super("mana");

		config.validate("amount");
		amount = config.getDouble("amount");
	}

	@Override
	public boolean isMet(PlayerData data) {
		return data.getRPG().getMana() >= amount;
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#mana#", "" + amount);
	}

	@Override
	public void whenCrafting(PlayerData data) {
		data.getRPG().giveMana(-amount);
	}
}
