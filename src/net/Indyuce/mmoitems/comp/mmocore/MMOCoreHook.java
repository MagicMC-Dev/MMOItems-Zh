package net.Indyuce.mmoitems.comp.mmocore;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.Indyuce.mmocore.MMOCore;
import net.Indyuce.mmocore.api.event.LevelUpEvent;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.StatType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.comp.rpg.RPGHandler;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class MMOCoreHook implements RPGHandler, Listener {

	/*
	 * called when MMOItems enables
	 */
	public MMOCoreHook() {

		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);

		/*
		 * register custom damage
		 */
		MMOCore.plugin.damage.registerHandler(new MMOCoreDamageHandler());
	}

	@Override
	public boolean canBeDamaged(Entity entity) {
		return true;
	}

	@Override
	public void refreshStats(net.Indyuce.mmoitems.api.player.PlayerData data) {
		PlayerData rpgdata = PlayerData.get(data.getPlayer());
		rpgdata.getStats().getInstance(StatType.MAX_MANA).addAttribute("MMOItems", data.getStats().getStat(ItemStat.MAX_MANA));
		rpgdata.getStats().getInstance(StatType.HEALTH_REGENERATION).addAttribute("MMOItems", data.getStats().getStat(ItemStat.REGENERATION));
	}

	@Override
	public RPGPlayer getInfo(net.Indyuce.mmoitems.api.player.PlayerData data) {
		return new MMOCoreRPGPlayer(data);
	}

	@EventHandler
	public void a(LevelUpEvent event) {
		net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayer()).scheduleDelayedInventoryUpdate();
	}

	public class MMOCoreRPGPlayer extends RPGPlayer {
		private PlayerData data;

		public MMOCoreRPGPlayer(net.Indyuce.mmoitems.api.player.PlayerData playerData) {
			super(playerData);

			data = PlayerData.get(playerData.getPlayer());
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
			return data.getStellium();
		}

		@Override
		public void setMana(double value) {
			data.setMana(value);
		}

		@Override
		public void setStamina(double value) {
			data.setStellium(value);
		}

		@Override
		public void giveMana(double value) {
			data.giveMana(value);
		}

		@Override
		public void giveStamina(double value) {
			data.giveStellium(value);
		}
	}
}