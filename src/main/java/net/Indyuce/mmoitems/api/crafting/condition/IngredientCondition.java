package net.Indyuce.mmoitems.api.crafting.condition;

import net.Indyuce.mmoitems.api.player.PlayerData;

public class IngredientCondition extends Condition {
	public IngredientCondition() {
		super("ingredient");
	}

	@Override
	public boolean isMet(PlayerData data) {
		return false;
	}

	@Override
	public String formatDisplay(String string) {
		return "";
	}

	@Override
	public void whenCrafting(PlayerData data) {
	}
}
