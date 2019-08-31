package net.Indyuce.mmoitems.comp.rpg;

import java.util.logging.Level;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.comp.mmocore.MMOCoreHook;

public interface RPGHandler {
	public RPGPlayer getInfo(PlayerData data);

	public void refreshStats(PlayerData data);

	public enum PluginEnum {
		MMOCORE("MMOCore", MMOCoreHook.class),
		HEROES("Heroes", HeroesHook.class),
		SKILLAPI("SkillAPI", SkillAPIHook.class),
		RPGPLAYERLEVELING("RPGPlayerLeveling", RPGPlayerLevelingHook.class),
		BATTLELEVELS("BattleLevels", BattleLevelsHook.class),
		MCMMO("mcMMO", McMMOHook.class),
		MCRPG("McRPG", McRPGHook.class),
		SKILLS("Skills", SkillsHook.class);

		private Class<? extends RPGHandler> pluginClass;
		private String name;

		private PluginEnum(String name, Class<? extends RPGHandler> pluginClass) {
			this.pluginClass = pluginClass;
			this.name = name;
		}

		public RPGHandler load() {
			try {
				return pluginClass.newInstance();
			} catch (InstantiationException | IllegalAccessException exception) {
				MMOItems.plugin.getLogger().log(Level.WARNING, "Could not load compatibility for " + name);
				return new DefaultHook();
			}
		}

		public String getName() {
			return name;
		}
	}
}
