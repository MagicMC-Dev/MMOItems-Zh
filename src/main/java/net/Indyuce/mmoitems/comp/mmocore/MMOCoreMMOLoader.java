package net.Indyuce.mmoitems.comp.mmocore;

import java.util.Optional;

import org.bukkit.configuration.ConfigurationSection;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.block.BlockType;
import net.Indyuce.mmocore.api.droptable.condition.Condition;
import net.Indyuce.mmocore.api.droptable.dropitem.DropItem;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.experience.source.type.ExperienceSource;
import net.Indyuce.mmocore.api.load.MMOLoader;
import net.Indyuce.mmocore.api.quest.objective.Objective;
import net.Indyuce.mmocore.api.quest.trigger.Trigger;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.block.CustomBlock;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.mmogroup.mmolib.api.util.AltChar;
import net.Indyuce.mmoitems.comp.mmocore.crafting.ExperienceCraftingTrigger;
import net.Indyuce.mmoitems.comp.mmocore.crafting.ProfessionCondition;
import net.Indyuce.mmoitems.comp.mmocore.load.GetMMOItemObjective;
import net.Indyuce.mmoitems.comp.mmocore.load.MMOItemDropItem;
import net.Indyuce.mmoitems.comp.mmocore.load.MMOItemTrigger;
import net.Indyuce.mmoitems.comp.mmocore.load.MMOItemsBlockType;
import net.Indyuce.mmoitems.comp.mmocore.load.MineMIBlockExperienceSource;
import net.Indyuce.mmoitems.comp.mmocore.load.SmeltMMOItemExperienceSource;
import net.mmogroup.mmolib.api.MMOLineConfig;

public class MMOCoreMMOLoader extends MMOLoader {

	/*
	 * called when MMOItems loads
	 */
	public MMOCoreMMOLoader() {
		MMOCore.plugin.loadManager.registerLoader(this);
		MMOCore.plugin.mineManager.registerBlockType(
				block -> {
					Optional<CustomBlock> customBlock = MMOItems.plugin.getCustomBlocks().getFromBlock(block.getBlockData());
					return customBlock.isPresent() ? Optional.of(new MMOItemsBlockType(customBlock.get())) : Optional.empty();
				});

		/*
		 * register extra conditions for MMOItems crafting.
		 */
		MMOItems.plugin.getCrafting().registerCondition("profession", config -> new ProfessionCondition(config), new ConditionalDisplay(
				"&a" + AltChar.check + " Requires #level# in #profession#", "&c" + AltChar.cross + " Requires #level# in #profession#"));
		MMOItems.plugin.getCrafting().registerTrigger("exp", config -> new ExperienceCraftingTrigger(config));
	}

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

		if (config.getKey().equals("minemiblock"))
			return new MineMIBlockExperienceSource(profession, config);

		if (config.getKey().equalsIgnoreCase("smeltmmoitem"))
			return new SmeltMMOItemExperienceSource(profession, config);

		return null;
	}

	@Override
	public BlockType loadBlockType(MMOLineConfig config) {

		if (config.getKey().equalsIgnoreCase("miblock") || config.getKey().equals("mmoitemsblock") || config.getKey().equals("mmoitem")
				|| config.getKey().equals("mmoitems"))
			return new MMOItemsBlockType(config);

		return null;
	}
}