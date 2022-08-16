package net.Indyuce.mmoitems.api.crafting.condition;

import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class FoodCondition extends Condition {
	private final int amount;

	public FoodCondition(MMOLineConfig config) {
		super("food");

		config.validate("amount");
		amount = config.getInt("amount");
	}

	@Override
	public boolean isMet(PlayerData data) {
		if(!data.isOnline()) return false;
		return data.getPlayer().getFoodLevel() >= amount;
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#food#", "" + amount);
	}

	@Override
	public void whenCrafting(PlayerData data) {
		if(!data.isOnline()) return;
		data.getPlayer().setFoodLevel(Math.max(0, data.getPlayer().getFoodLevel() - amount));
	}
}
