package net.Indyuce.mmoitems.comp.mmocore;

import io.lumine.mythic.lib.api.MMOLineConfig;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
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
import net.Indyuce.mmoitems.comp.mmocore.crafting.AttributeCondition;
import net.Indyuce.mmoitems.comp.mmocore.crafting.ExperienceCraftingTrigger;
import net.Indyuce.mmoitems.comp.mmocore.crafting.ProfessionCondition;
import net.Indyuce.mmoitems.comp.mmocore.crafting.StelliumCondition;
import net.Indyuce.mmoitems.comp.mmocore.load.*;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Optional;

public class MMOCoreMMOLoader extends MMOLoader {

    private static final ItemStat MANA_REGENERATION = new DoubleStat("MANA_REGENERATION", VersionMaterial.LAPIS_LAZULI.toMaterial(), "Mana Regeneration",
            new String[]{"Increases mana regen."});
    private static final ItemStat MAX_STAMINA = new DoubleStat("MAX_STAMINA", VersionMaterial.LIGHT_BLUE_DYE.toMaterial(), "Max Stamina",
            new String[]{"Adds stamina to your max stamina bar."});
    private static final ItemStat STAMINA_REGENERATION = new DoubleStat("STAMINA_REGENERATION", VersionMaterial.LIGHT_BLUE_DYE.toMaterial(),
            "Stamina Regeneration", new String[]{"Increases stamina regen."});
    private static final ItemStat ADDITIONAL_EXPERIENCE = new DoubleStat("ADDITIONAL_EXPERIENCE", VersionMaterial.EXPERIENCE_BOTTLE.toMaterial(),
            "Additional Experience", new String[]{"Additional MMOCore main class experience in %."});
    private static final ItemStat HEALTH_REGENERATION = new DoubleStat("HEALTH_REGENERATION", Material.BREAD, "Health Regeneration",
            new String[]{"Increases MMOCore health regen.", "In %."});

    /*
     * called when MMOItems loads
     */
    public MMOCoreMMOLoader() {
        MMOCore.plugin.loadManager.registerLoader(this);
        MMOCore.plugin.mineManager.registerBlockType(block -> {
            Optional<CustomBlock> customBlock = MMOItems.plugin.getCustomBlocks().getFromBlock(block.getBlockData());
            return customBlock.map(MMOItemsBlockType::new);
        });

        MMOItems.plugin.getStats().register(HEALTH_REGENERATION);
        MMOItems.plugin.getStats().register(MANA_REGENERATION);
        MMOItems.plugin.getStats().register(MAX_STAMINA);
        MMOItems.plugin.getStats().register(STAMINA_REGENERATION);
        MMOItems.plugin.getStats().register(ADDITIONAL_EXPERIENCE);

        /*
         * register extra conditions for MMOItems crafting.
         */
        MMOItems.plugin.getCrafting().registerCondition("profession", ProfessionCondition::new,
                new ConditionalDisplay(
                        "&a" + AltChar.check + " Requires #level# in #profession#",
                        "&c" + AltChar.cross + " Requires #level# in #profession#"));
        MMOItems.plugin.getCrafting().registerCondition("stellium", StelliumCondition::new,
                new ConditionalDisplay(
                        "&a" + AltChar.check + " Requires #stellium# Stellium",
                        "&c" + AltChar.cross + " Requires #stellium# Stellium"));
        MMOItems.plugin.getCrafting().registerCondition("attribute", AttributeCondition::new,
                new ConditionalDisplay(
                        "&a" + AltChar.check + " Requires #points# #attribute#",
                        "&c" + AltChar.cross + " Requires #points# #attribute#"));
        MMOItems.plugin.getCrafting().registerTrigger("exp", ExperienceCraftingTrigger::new);
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

        if (config.getKey().equals("mmoitem") || config.getKey().equals("mmoitemtemplate"))
            return new ItemTemplateDropItem(config);

        if (config.getKey().equals("miloot"))
            return new RandomItemDropItem(config);

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