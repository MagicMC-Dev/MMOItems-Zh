package net.Indyuce.mmoitems.comp.eco;

import io.lumine.mythic.lib.api.MMOLineConfig;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.condition.Condition;
import net.Indyuce.mmoitems.api.player.PlayerData;
import org.apache.commons.lang.Validate;

public class MoneyCondition extends Condition {
	private final double amount;

	public MoneyCondition(MMOLineConfig config) {
		super("money");

		Validate.isTrue(MMOItems.plugin.hasEconomy(), "No economy plugin found");
		config.validateKeys("amount");
		amount = config.getDouble("amount");
	}

	@Override
	public boolean isMet(PlayerData data) {
		return MMOItems.plugin.getVault().getEconomy().has(data.getPlayer(), amount);
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#money#", String.valueOf(amount));
	}

	@Override
	public void whenCrafting(PlayerData data) {
		MMOItems.plugin.getVault().getEconomy().withdrawPlayer(data.getPlayer(), amount);
	}

	public double getAmount() {
		return amount;
	}
}
