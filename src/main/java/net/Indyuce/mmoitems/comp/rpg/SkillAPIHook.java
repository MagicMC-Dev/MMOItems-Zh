package net.Indyuce.mmoitems.comp.rpg;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.PlayerLevelUpEvent;
import com.sucy.skill.api.event.SkillDamageEvent;
import com.sucy.skill.api.player.PlayerData;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class SkillAPIHook implements RPGHandler, Listener {
	public SkillAPIHook() {
		Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
	}

	@Override
	public RPGPlayer getInfo(net.Indyuce.mmoitems.api.player.PlayerData data) {
		return new SkillAPIPlayer(data);
	}

	@EventHandler
	public void a(SkillDamageEvent event) {

		if (event.getDamager() instanceof Player)
			event.setDamage(event.getDamage() * (1 + net.Indyuce.mmoitems.api.player.PlayerData.get((Player) event.getDamager()).getStats().getStat(ItemStat.MAGICAL_DAMAGE) / 100));

		if (event.getTarget() instanceof Player)
			event.setDamage(event.getDamage() * (1 - net.Indyuce.mmoitems.api.player.PlayerData.get((Player) event.getTarget()).getStats().getStat(ItemStat.MAGIC_DAMAGE_REDUCTION) / 100));
	}

	@EventHandler
	public void b(PlayerLevelUpEvent event) {
		net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayerData().getPlayer()).scheduleDelayedInventoryUpdate();
	}

	@Override
	public void refreshStats(net.Indyuce.mmoitems.api.player.PlayerData data) {
	}

	public class SkillAPIPlayer extends RPGPlayer {
		private final PlayerData rpgdata;

		public SkillAPIPlayer(net.Indyuce.mmoitems.api.player.PlayerData playerData) {
			super(playerData);

			rpgdata = SkillAPI.getPlayerData(playerData.getPlayer());
		}

		@Override
		public int getLevel() {
			return rpgdata.hasClass() ? rpgdata.getMainClass().getLevel() : 0;
		}

		@Override
		public String getClassName() {
			return rpgdata.hasClass() ? rpgdata.getMainClass().getData().getName() : "";
		}

		@Override
		public double getMana() {
			return rpgdata.hasClass() ? rpgdata.getMana() : 0;
		}

		@Override
		public double getStamina() {
			return getPlayer().getFoodLevel();
		}

		@Override
		public void setMana(double value) {
			if (rpgdata.hasClass())
				rpgdata.setMana(value);
		}

		@Override
		public void setStamina(double value) {
			getPlayer().setFoodLevel((int) value);
		}
	}
}