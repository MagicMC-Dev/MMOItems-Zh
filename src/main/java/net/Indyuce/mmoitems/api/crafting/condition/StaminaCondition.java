package net.Indyuce.mmoitems.api.crafting.condition;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.AltChar;

public class StaminaCondition extends Condition {
	private double amount;

	public StaminaCondition() {
		super("stamina");

		setDisplay(new ConditionalDisplay("&a" + AltChar.check + " Requires #stamina# Stamina", "&c" + AltChar.cross + " Requires #stamina# Stamina"));
	}

	@Override
	public Condition load(String[] args) {
		Validate.notEmpty(args, "This ingredient requires arguments");

		StaminaCondition condition = new StaminaCondition();
		condition.amount = Double.parseDouble(args[0]);
		condition.setDisplay(getDisplay());

		return condition;
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
