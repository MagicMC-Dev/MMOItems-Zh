package net.Indyuce.mmoitems.comp.mmocore;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLineConfig;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.comp.mmocore.crafting.ExperienceCraftingTrigger;
import net.Indyuce.mmoitems.comp.mmocore.crafting.ProfessionCondition;
import net.Indyuce.mmoitems.comp.mmocore.load.GetMMOItemObjective;
import net.Indyuce.mmoitems.comp.mmocore.load.MMOItemDropItem;
import net.Indyuce.mmoitems.comp.mmocore.load.MMOItemTrigger;
import net.Indyuce.mmoitems.comp.mmocore.load.SmeltMMOItemExperienceSource;

public class MMOCoreMMOLoader implements MMOLoader {

	/*
	 * called when MMOItems loads
	 */
	public MMOCoreMMOLoader() {
		MMOCore.plugin.loadManager.registerLoader(this);

		/*
		 * register extra conditions for MMOItems crafting.
		 */
		MMOItems.plugin.getCrafting().registerCondition(new ProfessionCondition());
		MMOItems.plugin.getCrafting().registerTrigger(new ExperienceCraftingTrigger());}

	@Override
	public Condition loadCondition(MMOLineConfig config) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Trigger loadTrigger(MMOLineConfig config) {

		if (config.getKey().equals("mmoitem"))
			return new MMOItemTrigger(config);

		return null;
	}

	@Override
	public DropItem loadDropItem(MMOLineConfig config) {

		if (config.getKey().equals("mmoitem"))
			return new MMOItemDropItem(config);

		return null;
	}

	@Override
	public Objective loadObjective(MMOLineConfig config, ConfigurationSection section) {

		if (config.getKey().equals("getmmoitem"))
			return new GetMMOItemObjective(section, config);

		return null;
	}

	@Override
	public ExperienceSource<?> loadExperienceSource(MMOLineConfig config, Profession profession) {

		if (config.getKey().equalsIgnoreCase("smeltmmoitem"))
			return new SmeltMMOItemExperienceSource(profession, config);

		return null;
	}
}