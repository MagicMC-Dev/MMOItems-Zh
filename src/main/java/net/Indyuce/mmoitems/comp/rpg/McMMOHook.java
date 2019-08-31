package net.Indyuce.mmoitems.comp.rpg;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.api.exceptions.McMMOPlayerNotFoundException;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelDownEvent;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;

public class McMMOHook implements RPGHandler, Listener {
	public McMMOHook() {
		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	@EventHandler
	public void a(McMMOPlayerLevelUpEvent event) {
		PlayerData.get(event.getPlayer()).scheduleDelayedInventoryUpdate();
	}

	@EventHandler
	public void b(McMMOPlayerLevelDownEvent event) {
		PlayerData.get(event.getPlayer()).scheduleDelayedInventoryUpdate();
	}

	@Override
	public RPGPlayer getInfo(PlayerData data) {
		return new McMMOPlayer(data);
	}

	@Override
	public void refreshStats(PlayerData data) {
	}

	public class McMMOPlayer extends RPGPlayer {
		public McMMOPlayer(PlayerData playerData) {
			super(playerData);
		}

		@Override
		public int getLevel() {
			try {
				return ExperienceAPI.getPowerLevel(getPlayer());
			} catch (McMMOPlayerNotFoundException exception) {
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