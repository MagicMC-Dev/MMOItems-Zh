package net.Indyuce.mmoitems.comp.eco;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.condition.Condition;
import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class MoneyCondition extends Condition {
	private final double amount;

	public MoneyCondition(MMOLineConfig config) {
		super("money");

		config.validate("amount");
		amount = config.getDouble("amount");
	}

	@Override
	public boolean isMet(PlayerData data) {
		if(!data.isOnline()) return false;
		return MMOItems.plugin.getVault().getEconomy().has(data.getPlayer(), amount);
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#money#", "" + amount);
	}

	@Override
	public void whenCrafting(PlayerData data) {
		if(!data.isOnline()) return;
		MMOItems.plugin.getVault().getEconomy().withdrawPlayer(data.getPlayer(), amount);
	}
}
