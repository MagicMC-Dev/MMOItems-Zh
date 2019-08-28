package net.Indyuce.mmoitems.api.crafting.condition;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.AltChar;

public class FoodCondition extends Condition {
	private int amount;

	public FoodCondition() {
		super("food");

		setDisplay(new ConditionalDisplay("&a" + AltChar.check + " Requires #food# Food", "&c" + AltChar.cross + " Requires #food# Food"));
	}

	@Override
	public Condition load(String[] args) {
		Validate.notEmpty(args, "This ingredient requires arguments");

		FoodCondition condition = new FoodCondition();
		condition.amount = Integer.parseInt(args[0]);
		condition.setDisplay(getDisplay());

		return condition;
	}

	@Override
	public boolean isMet(PlayerData data) {
		return data.getPlayer().getFoodLevel() >= amount;
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#food#", "" + amount);
	}

	@Override
	public void whenCrafting(PlayerData data) {
		data.getPlayer().setFoodLevel(Math.max(0, data.getPlayer().getFoodLevel() - amount));
	}
}
