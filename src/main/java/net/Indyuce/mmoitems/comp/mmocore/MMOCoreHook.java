package net.Indyuce.mmoitems.comp.mmocore;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.PlayerChangeClassEvent;
import net.Indyuce.mmocore.api.event.PlayerLevelUpEvent;
import net.Indyuce.mmocore.api.experience.Profession;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.attribute.PlayerAttribute;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.comp.mmocore.stat.Required_Attribute;
import net.Indyuce.mmoitems.comp.mmocore.stat.Required_Profession;
import net.Indyuce.mmoitems.comp.rpg.RPGHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MMOCoreHook implements RPGHandler, Listener {

	/**
	 * Called when MMOItems enables
	 */
	public MMOCoreHook() {
		/*
		 * only works when the server is reloaded. needs /reload when changing
		 * attributes or professions to refresh MMOItems stats
		 */
		for (PlayerAttribute attribute : MMOCore.plugin.attributeManager.getAll())
			MMOItems.plugin.getStats().register(new Required_Attribute(attribute));
		for (Profession profession : MMOCore.plugin.professionManager.getAll())
			MMOItems.plugin.getStats().register(new Required_Profession(profession));
	}

	@Override
	public void refreshStats(net.Indyuce.mmoitems.api.player.PlayerData data) {
	}

	@Override
	public RPGPlayer getInfo(net.Indyuce.mmoitems.api.player.PlayerData data) {
		return new MMOCoreRPGPlayer(data);
	}

	@EventHandler
	public void updateInventoryOnLevelUp(PlayerLevelUpEvent event) {
		net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
	}

	@EventHandler
	public void updateInventoryOnClassChange(PlayerChangeClassEvent event) {
		net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
	}

	/**
	 * Removing this as it is causing issues when players log on for the first time.
	 * Right after MMOCore loads the player data, MMOItems player data is not loaded yet
	 * so net.Indyuce.mmoitems.api.player.PlayerData.get() returns null so this can't work
	 */
	/*@EventHandler
	public void updateInventoryOnPlayerDataLoad(PlayerDataLoadEvent event) {
		net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayer()).getInventory().scheduleUpdate();
	}*/

	public static class MMOCoreRPGPlayer extends RPGPlayer {
		private final PlayerData data;

		public MMOCoreRPGPlayer(net.Indyuce.mmoitems.api.player.PlayerData playerData) {
			super(playerData);

			data = PlayerData.get(playerData.getUniqueId());
		}

		public PlayerData getData() {
			return data;
		}

		@Override
		public int getLevel() {
			return data.getLevel();
		}

		@Override
		public String getClassName() {
			return data.getProfess().getName();
		}

		@Override
		public double getMana() {
			return data.getMana();
		}

		@Override
		public double getStamina() {
			return data.getStamina();
		}

		@Override
		public void setMana(double value) {
			data.setMana(value);
		}

		@Override
		public void setStamina(double value) {
			data.setStamina(value);
		}

		@Override
		public void giveMana(double value) {
			data.giveMana(value);
		}

		@Override
		public void giveStamina(double value) {
			data.giveStamina(value);
		}
	}
}