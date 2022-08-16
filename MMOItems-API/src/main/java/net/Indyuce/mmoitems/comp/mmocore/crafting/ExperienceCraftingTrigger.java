package net.Indyuce.mmoitems.comp.mmocore.crafting;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.experience.EXPSource;
import net.Indyuce.mmocore.experience.Profession;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmoitems.api.crafting.trigger.Trigger;
import io.lumine.mythic.lib.api.MMOLineConfig;

public class ExperienceCraftingTrigger extends Trigger {
	private final Profession profession;
	private final int amount;

	public ExperienceCraftingTrigger(MMOLineConfig config) {
		super("exp");

		config.validate("profession", "amount");

		amount = config.getInt("amount");

		String id = config.getString("profession").toLowerCase().replace("_", "-");
		if (!id.equalsIgnoreCase("main")) {
			Validate.isTrue(MMOCore.plugin.professionManager.has(id), "Could not find profession " + id);
			profession = MMOCore.plugin.professionManager.get(id);
		} else
			profession = null;
	}

	@Override
	public void whenCrafting(net.Indyuce.mmoitems.api.player.PlayerData data) {
		if (profession == null)
			PlayerData.get(data.getUniqueId()).giveExperience(amount, EXPSource.SOURCE);
		else
			PlayerData.get(data.getUniqueId()).getCollectionSkills().giveExperience(profession, amount, EXPSource.SOURCE);
	}
}
