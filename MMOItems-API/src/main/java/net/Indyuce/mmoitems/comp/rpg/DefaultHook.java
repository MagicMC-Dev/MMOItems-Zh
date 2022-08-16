package net.Indyuce.mmoitems.comp.rpg;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;

public class DefaultHook implements RPGHandler, Listener {

	@Override
	public void refreshStats(PlayerData data) {
	}

	@EventHandler
	public void a(PlayerLevelChangeEvent event) {
		PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
	}

	@Override
	public RPGPlayer getInfo(PlayerData data) {
		return new DefaultRPGPlayer(data);
	}

	public static class DefaultRPGPlayer extends RPGPlayer {
		public DefaultRPGPlayer(PlayerData playerData) {
			super(playerData);
		}

		@Override
		public int getLevel() {
			return getPlayer().getLevel();
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