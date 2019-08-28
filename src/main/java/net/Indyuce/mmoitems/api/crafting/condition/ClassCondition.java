package net.Indyuce.mmoitems.api.crafting.condition;

import java.util.ArrayList;
import java.util.List;

import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.util.AltChar;

public class ClassCondition extends Condition {
	private List<String> classes = new ArrayList<>();

	public ClassCondition() {
		super("class");

		setDisplay(new ConditionalDisplay("&a" + AltChar.check + " Required Class: #class#", "&c" + AltChar.cross + " Required Class: #class#"));
	}

	@Override
	public Condition load(String[] args) {
		ClassCondition condition = new ClassCondition();
		for (String permission : args)
			condition.classes.add(permission);
		condition.setDisplay(getDisplay());

		return condition;
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
