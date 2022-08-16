package net.Indyuce.mmoitems.comp.rpg;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import us.eunoians.mcrpg.api.events.mcrpg.McRPGPlayerLevelChangeEvent;
import us.eunoians.mcrpg.players.PlayerManager;

public class McRPGHook implements RPGHandler, Listener {

	@EventHandler
	public void a(McRPGPlayerLevelChangeEvent event) {
		PlayerData.get(event.getMcRPGPlayer().getOfflineMcRPGPlayer()).getInventory().scheduleUpdate();
	}

	@Override
	public RPGPlayer getInfo(PlayerData data) {
		return new McRPGPlayer(data);
	}

	@Override
	public void refreshStats(PlayerData data) {
	}

	public static class McRPGPlayer extends RPGPlayer {
		public McRPGPlayer(PlayerData playerData) {
			super(playerData);
		}

		@Override
		public int getLevel() {
			try {
				return PlayerManager.getPlayer(getPlayer().getUniqueId()).getPowerLevel();
			} catch (Exception exception) {
				return 0;
			}
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