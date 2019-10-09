package net.Indyuce.mmoitems.api.crafting.condition;

import net.Indyuce.mmoitems.api.player.PlayerData;

public class IngredientCondition extends Condition {
	public IngredientCondition() {
		super("has-ingredients");
	}

	@Override
	public Condition load(String[] args) {
		IngredientCondition condition = new IngredientCondition();
		return condition;
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
