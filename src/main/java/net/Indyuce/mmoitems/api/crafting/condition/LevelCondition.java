package net.Indyuce.mmoitems.api.crafting.condition;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.MMOLineConfig;

public class LevelCondition extends Condition {
	private final int level;

	public LevelCondition(MMOLineConfig config) {
		super("level");

		config.validate("level");
		level = config.getInt("level");
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
