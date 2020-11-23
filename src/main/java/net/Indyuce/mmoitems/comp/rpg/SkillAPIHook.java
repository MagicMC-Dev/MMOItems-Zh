package net.Indyuce.mmoitems.comp.rpg;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.PlayerLevelUpEvent;
import com.sucy.skill.api.event.SkillDamageEvent;
import com.sucy.skill.api.player.PlayerData;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.player.RPGPlayer;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageHandler;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.RegisteredAttack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;

public class SkillAPIHook implements RPGHandler, Listener, DamageHandler {
	private final Map<Integer, RegisteredAttack> damageInfo = new HashMap<>();

	public SkillAPIHook() {
		MMOLib.plugin.getDamage().registerHandler(this);
	}

	@Override
	public RPGPlayer getInfo(net.Indyuce.mmoitems.api.player.PlayerData data) {
		return new SkillAPIPlayer(data);
	}

	@Override
	public RegisteredAttack getDamage(Entity entity) {
		return damageInfo.get(entity.getEntityId());
	}

	@Override
	public boolean hasDamage(Entity entity) {
		return damageInfo.containsKey(entity.getEntityId());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void a(SkillDamageEvent event) {
		damageInfo.put(event.getTarget().getEntityId(), new RegisteredAttack(new AttackResult(event.getDamage(), DamageType.SKILL), event.getDamager()));

		if (event.getDamager() instanceof Player)
			event.setDamage(event.getDamage() * (1 + net.Indyuce.mmoitems.api.player.PlayerData.get((Player) event.getDamager()).getStats().getStat(ItemStats.MAGIC_DAMAGE) / 100));

		if (event.getTarget() instanceof Player)
			event.setDamage(event.getDamage() * (1 - net.Indyuce.mmoitems.api.player.PlayerData.get((Player) event.getTarget()).getStats().getStat(ItemStats.MAGIC_DAMAGE_REDUCTION) / 100));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void c(EntityDamageByEntityEvent event) {
		damageInfo.remove(event.getEntity().getEntityId());
	}

	@EventHandler
	public void b(PlayerLevelUpEvent event) {
		net.Indyuce.mmoitems.api.player.PlayerData.get(event.getPlayerData().getPlayer()).scheduleDelayedInventoryUpdate();
	}

	@Override
	public void refreshStats(net.Indyuce.mmoitems.api.player.PlayerData data) {
	}

	public static class SkillAPIPlayer extends RPGPlayer {
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