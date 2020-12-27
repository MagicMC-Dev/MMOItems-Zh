package net.Indyuce.mmoitems.comp.rpg;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.leothepro555.skills.database.managers.PlayerInfo;
import me.leothepro555.skills.events.SkillLevelUpEvent;
import me.leothepro555.skills.main.Skills;
import me.leothepro555.skilltype.ScalingType;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;

public class SkillsHook implements RPGHandler, Listener {

	@EventHandler
	public void a(SkillLevelUpEvent event) {
		OfflinePlayer player = event.getPlayer();
		if (player.isOnline())
			PlayerData.get(player).getInventory().scheduleUpdate();
	}

	@Override
	public void refreshStats(PlayerData data) {
	}

	@Override
	public RPGPlayer getInfo(PlayerData data) {
		return new SkillsPlayer(data);
	}

	public static class SkillsPlayer extends RPGPlayer {
		private final PlayerInfo info;

		public SkillsPlayer(PlayerData playerData) {
			super(playerData);

			info = Skills.get().getPlayerDataManager().loadPlayerInfo(playerData.getPlayer());
		}

		@Override
		public int getLevel() {
			return info.getLevel();
		}

		@Override
		public String getClassName() {
			return info.getSkill().getLanguageName().getDefault();
		}

		@Override
		public double getMana() {
			return info.getActiveStatType(ScalingType.ENERGY);
		}

		@Override
		public double getStamina() {
			return getPlayer().getFoodLevel();
		}

		@Override
		public void setMana(double value) {
			info.setActiveStatType(ScalingType.ENERGY, value);
		}

		@Override
		public void setStamina(double value) {
			getPlayer().setFoodLevel((int) value);
		}
	}
}