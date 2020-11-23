package net.Indyuce.mmoitems.comp.rpg;

import java.util.logging.Level;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.comp.mmocore.MMOCoreHook;

public interface RPGHandler {
	RPGPlayer getInfo(PlayerData data);

	void refreshStats(PlayerData data);

	enum PluginEnum {
		MMOCORE("MMOCore", MMOCoreHook.class),
		HEROES("Heroes", HeroesHook.class),
		SKILLAPI("SkillAPI", SkillAPIHook.class),
		RPGPLAYERLEVELING("RPGPlayerLeveling", RPGPlayerLevelingHook.class),
		RACESANDCLASSES("RacesAndClasses", RacesAndClassesHook.class),
		BATTLELEVELS("BattleLevels", BattleLevelsHook.class),
		MCMMO("mcMMO", McMMOHook.class),
		MCRPG("McRPG", McRPGHook.class),
		SKILLS("Skills", SkillsHook.class);

		private final Class<? extends RPGHandler> pluginClass;
		private final String name;

		PluginEnum(String name, Class<? extends RPGHandler> pluginClass) {
			this.pluginClass = pluginClass;
			this.name = name;
		}

		public RPGHandler load() {
			try {
				return pluginClass.newInstance();
			} catch (InstantiationException | IllegalAccessException exception) {
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
