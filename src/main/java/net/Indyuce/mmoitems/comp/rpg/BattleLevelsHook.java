package net.Indyuce.mmoitems.comp.rpg;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.robin.battlelevels.api.BattleLevelsAPI;
import me.robin.battlelevels.events.PlayerLevelUpEvent;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;

public class BattleLevelsHook implements RPGHandler, Listener {

	@EventHandler
	public void a(PlayerLevelUpEvent event) {
		PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
	}

	@Override
	public RPGPlayer getInfo(PlayerData data) {
		return new BattleLevelsPlayer(data);
	}

	@Override
	public void refreshStats(PlayerData data) {
	}

	public static class BattleLevelsPlayer extends RPGPlayer {
		public BattleLevelsPlayer(PlayerData playerData) {
			super(playerData);
		}

		@Override
		public int getLevel() {
			return BattleLevelsAPI.getLevel(getPlayer().getUniqueId());
		}

		@Override
		public String getClassName() {
			return "";
		}

		@Override
		public double getMana() {
			return getPlayer().getFoodLevel();
		}

		@Override
		public double getStamina() {
			return 0;
		}

		@Override
		public void setMana(double value) {
			getPlayer().setFoodLevel((int) value);
		}

		@Override
		public void setStamina(double value) {
		}
	}
}