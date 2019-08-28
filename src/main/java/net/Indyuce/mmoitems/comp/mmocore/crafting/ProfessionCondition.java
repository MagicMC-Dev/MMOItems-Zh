package net.Indyuce.mmoitems.comp.mmocore.crafting;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.condition.Condition;
import net.Indyuce.mmoitems.api.util.AltChar;

public class ProfessionCondition extends Condition {
	private Profession profession;
	private int level;

	public ProfessionCondition() {
		super("profession");
		setDisplay(new ConditionalDisplay("&a" + AltChar.check + " Requires #level# in #skill#", "&c" + AltChar.check + " Requires #level# in #skill#"));
	}

	@Override
	public ProfessionCondition load(String[] args) {
		try {
			String id = args[0].toLowerCase().replace("_", "-");
			Validate.isTrue(MMOCore.plugin.professionManager.has(id), "Could not find profession " + id);

			ProfessionCondition condition = new ProfessionCondition();
			condition.level = Integer.parseInt(args[1]);
			condition.profession = MMOCore.plugin.professionManager.get(id);
			condition.setDisplay(getDisplay());
			return condition;
		} catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
			return null;
		}
	}

	@Override
	public String formatDisplay(String string) {
		return string.replace("#level#", "" + level).replace("#skill#", profession.getName());
	}

	@Override
	public boolean isMet(net.Indyuce.mmoitems.api.player.PlayerData data) {
		return PlayerData.get(data.getUniqueId()).getCollectionSkills().getLevel(profession) >= level;
	}

	@Override
	public void whenCrafting(net.Indyuce.mmoitems.api.player.PlayerData data) {
	}
}
