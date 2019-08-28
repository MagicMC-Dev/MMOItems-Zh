package net.Indyuce.mmoitems.api.crafting.condition;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.AltChar;

public class ManaCondition extends Condition {
	private double amount;

	public ManaCondition() {
		super("mana");

		setDisplay(new ConditionalDisplay("&a" + AltChar.check + " Requires #mana# Mana", "&c" + AltChar.cross + " Requires #mana# Mana"));
	}

	@Override
	public Condition load(String[] args) {
		Validate.notEmpty(args, "This ingredient requires arguments");

		ManaCondition condition = new ManaCondition();
		condition.amount = Double.parseDouble(args[0]);
		condition.setDisplay(getDisplay());

		return condition;
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
