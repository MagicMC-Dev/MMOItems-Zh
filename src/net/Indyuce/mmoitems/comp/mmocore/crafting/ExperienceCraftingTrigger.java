package net.Indyuce.mmoitems.comp.mmocore.crafting;

import org.apache.commons.lang.Validate;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmoitems.api.crafting.trigger.Trigger;

public class ExperienceCraftingTrigger extends Trigger {
	private int amount;
	private Profession profession;

	public ExperienceCraftingTrigger() {
		super("exp");
	}

	@Override
	public Trigger load(String[] args) {
		Validate.isTrue(args.length > 1, "There must be 2 args exactly, profession (or main) and exp");

		ExperienceCraftingTrigger trigger = new ExperienceCraftingTrigger();
		if (!args[0].equalsIgnoreCase("main")) {
			String id = args[0].toLowerCase().replace("_", "-");
			Validate.isTrue(MMOCore.plugin.professionManager.has(id), "Could not find profession " + id);
			trigger.profession = MMOCore.plugin.professionManager.get(id);
		}
		trigger.amount = Integer.parseInt(args[1]);

		return trigger;
	}

	@Override
	public void whenCrafting(net.Indyuce.mmoitems.api.player.PlayerData data) {
		if (profession == null)
			PlayerData.get(data.getUniqueId()).giveExperience(amount);
		else
			PlayerData.get(data.getUniqueId()).getCollectionSkills().giveExperience(profession, amount);
	}
}
