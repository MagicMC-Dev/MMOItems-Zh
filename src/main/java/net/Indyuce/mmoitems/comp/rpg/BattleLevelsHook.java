package net.Indyuce.mmoitems.comp.rpg;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import me.robin.battlelevels.api.BattleLevelsAPI;
import me.robin.battlelevels.events.PlayerLevelUpEvent;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;

public class BattleLevelsHook implements RPGHandler, Listener {
	public BattleLevelsHook() {
		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	@EventHandler
	public void a(PlayerLevelUpEvent event) {
		PlayerData.get(event.getPlayer()).scheduleDelayedInventoryUpdate();
	}

	@Override
	public RPGPlayer getInfo(PlayerData data) {
		return new BattleLevelsPlayer(data);
	}

	@Override
	public void refreshStats(PlayerData data) {
	}

	public class BattleLevelsPlayer extends RPGPlayer {
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