package net.Indyuce.mmoitems.api.crafting.condition;

import java.util.Arrays;
import java.util.List;

import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class ClassCondition extends Condition {
	private final List<String> classes;

	public ClassCondition(MMOLineConfig config) {
		super("class");

		config.validate("list");
		classes = Arrays.asList(config.getString("list").split(","));
	}

	@Override
	public boolean isMet(PlayerData data) {
		return classes.contains(data.getRPG().getClassName());
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#class#", String.join(", ", classes));
	}

	@Override
	public void whenCrafting(PlayerData data) {
	}
}
