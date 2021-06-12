package net.Indyuce.mmoitems.comp.rpg;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.api.event.SkillLevelUpEvent;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;

public class AureliumSkillsHook implements RPGHandler, Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void a(SkillLevelUpEvent event) {
		PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
	}

	@Override
	public void refreshStats(PlayerData data) {
//		com.archyx.aureliumskills.data.PlayerData info = ((SkillsPlayer) data.getRPG()).info;
//		info.removeStatModifier("MMOItemsMana");
//		info.addStatModifier(new StatModifier("MMOItemsMana", Stats., 0));
	}

	@Override
	public RPGPlayer getInfo(PlayerData data) {
		return new SkillsPlayer(data);
	}

	public static class SkillsPlayer extends RPGPlayer {
		private final com.archyx.aureliumskills.data.PlayerData info;

		public SkillsPlayer(PlayerData playerData) {
			super(playerData);

			AureliumSkills plugin = (AureliumSkills) Bukkit.getPluginManager().getPlugin("AureliumSkills");
			info = plugin.getPlayerManager().getPlayerData(playerData.getUniqueId());
		}

		@Override
		public int getLevel() {
			return info.getPowerLevel();
		}

		@Override
		public String getClassName() {
			return "";
		}

		@Override
		public double getMana() {
			return info.getMana();
		}

		@Override
		public double getStamina() {
			return getPlayer().getFoodLevel();
		}

		@Override
		public void setMana(double value) {
			info.setMana(value);
		}

		@Override
		public void setStamina(double value) {
			getPlayer().setFoodLevel((int) value);
		}
	}
}