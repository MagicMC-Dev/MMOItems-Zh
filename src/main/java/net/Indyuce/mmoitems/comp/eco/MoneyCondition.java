package net.Indyuce.mmoitems.comp.eco;

import net.Indyuce.mmoitems.api.crafting.condition.Condition;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.milkbowl.vault.economy.Economy;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class MoneyCondition extends Condition {
	private final double amount;
	private final Economy economy;

	public MoneyCondition(Economy economy, MMOLineConfig config) {
		super("money");

		config.validate("amount");
		amount = config.getDouble("amount");
		this.economy = economy;
	}

	@Override
	public boolean isMet(PlayerData data) {
		return economy.has(data.getPlayer(), amount);
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#money#", "" + amount);
	}

	@Override
	public void whenCrafting(PlayerData data) {
		economy.withdrawPlayer(data.getPlayer(), amount);
	}
}
