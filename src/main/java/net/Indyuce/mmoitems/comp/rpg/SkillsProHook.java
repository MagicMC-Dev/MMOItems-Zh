package net.Indyuce.mmoitems.comp.rpg;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.skills.api.events.SkillLevelUpEvent;
import org.skills.data.managers.SkilledPlayer;
import org.skills.main.SkillsPro;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;

public class SkillsProHook implements RPGHandler, Listener {

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
		private final SkilledPlayer info;

		public SkillsPlayer(PlayerData playerData) {
			super(playerData);

			info = SkillsPro.get().getPlayerDataManager().getData(playerData.getUniqueId());
		}

		@Override
		public int getLevel() {
			return info.getLevel();
		}

		@Override
		public String getClassName() {
			return ChatColor.stripColor(info.getSkill().getDisplayName());
		}

		@Override
		public double getMana() {
			return info.getEnergy();
		}

		@Override
		public double getStamina() {
			return getPlayer().getFoodLevel();
		}

		@Override
		public void setMana(double value) {
			info.setEnergy(value);
		}

		@Override
		public void setStamina(double value) {
			getPlayer().setFoodLevel((int) value);
		}
	}
}