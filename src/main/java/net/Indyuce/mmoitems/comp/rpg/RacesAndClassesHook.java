package net.Indyuce.mmoitems.comp.rpg;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import de.tobiyas.racesandclasses.eventprocessing.events.leveling.LevelDownEvent;
import de.tobiyas.racesandclasses.eventprocessing.events.leveling.LevelUpEvent;
import de.tobiyas.racesandclasses.playermanagement.player.RaCPlayer;
import de.tobiyas.racesandclasses.playermanagement.player.RaCPlayerManager;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class RacesAndClassesHook implements RPGHandler, Listener {
	public RacesAndClassesHook() {
		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	@Override
	public void refreshStats(PlayerData data) {
		RaCPlayer info = ((RacePlayer) data.getRPG()).info;
		info.getManaManager().removeMaxManaBonus("MMOItems");
		info.getManaManager().addMaxManaBonus("MMOItems", data.getStats().getStat(ItemStat.MAX_MANA));
	}

	@Override
	public RPGPlayer getInfo(PlayerData data) {
		return new RacePlayer(data);
	}

	/*
	 * update the player's inventory whenever he levels up since it could change its
	 * current stat requirements
	 */
	@EventHandler
	public void a(LevelUpEvent event) {
		PlayerData.get(event.getPlayer()).scheduleDelayedInventoryUpdate();
	}

	@EventHandler
	public void b(LevelDownEvent event) {
		PlayerData.get(event.getPlayer()).scheduleDelayedInventoryUpdate();
	}

	public class RacePlayer extends RPGPlayer {
		private final RaCPlayer info;

		public RacePlayer(PlayerData playerData) {
			super(playerData);

			info = RaCPlayerManager.get().getPlayer(playerData.getUniqueId());
		}

		@Override
		public int getLevel() {
			return info.getCurrentLevel();
		}

		@Override
		public String getClassName() {
			return info.getclass().getDisplayName();
		}

		@Override
		public double getMana() {
			return info.getCurrentMana();
		}

		@Override
		public double getStamina() {
			return info.getPlayer().getFoodLevel();
		}

		@Override
		public void setMana(double value) {
			info.getManaManager().fillMana(value - info.getManaManager().getCurrentMana());
		}

		@Override
		public void setStamina(double value) {
			info.getPlayer().setFoodLevel((int) value);
		}
	}
}
