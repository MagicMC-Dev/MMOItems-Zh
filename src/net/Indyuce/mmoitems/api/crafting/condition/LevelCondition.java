package net.Indyuce.mmoitems.api.crafting.condition;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.AltChar;

public class LevelCondition extends Condition {
	private int level;

	public LevelCondition() {
		super("level");

		setDisplay(new ConditionalDisplay("&a" + AltChar.check + " Requires Level #level#", "&c" + AltChar.cross + " Requires Level #level#"));
	}

	@Override
	public Condition load(String[] args) {
		Validate.notEmpty(args, "This ingredient requires arguments");

		LevelCondition condition = new LevelCondition();
		condition.level = Integer.parseInt(args[0]);
		condition.setDisplay(getDisplay());

		return condition;
	}

	@Override
	public boolean isMet(PlayerData data) {
		return data.getRPG().getLevel() >= level;
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#level#", "" + level);
	}

	@Override
	public void whenCrafting(PlayerData data) {
	}
}
