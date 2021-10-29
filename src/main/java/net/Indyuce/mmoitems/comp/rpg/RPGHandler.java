package net.Indyuce.mmoitems.comp.rpg;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.comp.mmocore.MMOCoreHook;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

public interface RPGHandler {

    /**
     * A RPGPlayer is a class used to retrieve all the rpg
     * information of a player, like its class, level, mana
     * stamina. It's also used to give or take mana/stamina
     * to handle ability costs.
     *
     * @param data Player to retrieve rpg info from
     * @return A new RPGPlayer for the given player
     */
    RPGPlayer getInfo(PlayerData data);

    /**
     * Called everytime the player's inventory updates. This
     * method should update the rpg stats like Max Mana which
     * are normally given by items.
     *
     * @param data Player to update
     */
    void refreshStats(PlayerData data);

    enum PluginEnum {
        MMOCORE("MMOCore", MMOCoreHook.class),
        HEROES("Heroes", HeroesHook.class),
        PROSKILLAPI("ProSkillAPI", ProSkillAPIHook.class),
        SKILLAPI("SkillAPI", SkillAPIHook.class),
        RPGPLAYERLEVELING("RPGPlayerLeveling", RPGPlayerLevelingHook.class),
        RACESANDCLASSES("RacesAndClasses", RacesAndClassesHook.class),
        BATTLELEVELS("BattleLevels", BattleLevelsHook.class),
        MCMMO("mcMMO", McMMOHook.class),
        MCRPG("McRPG", McRPGHook.class),
        SKILLS("Skills", SkillsHook.class),
        AURELIUM_SKILLS("AureliumSkills", AureliumSkillsHook.class),
        SKILLSPRO("SkillsPro", SkillsProHook.class);

        private final Class<? extends RPGHandler> pluginClass;
        private final String name;

        PluginEnum(String name, Class<? extends RPGHandler> pluginClass) {
            this.pluginClass = pluginClass;
            this.name = name;
        }

        public RPGHandler load() {
            try {
                return pluginClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING,
                        "Could not initialize RPG plugin compatibility with " + name + ": " + exception.getMessage());
                return new DefaultHook();
            }
        }

        public String getName() {
            return name;
        }
    }
}
