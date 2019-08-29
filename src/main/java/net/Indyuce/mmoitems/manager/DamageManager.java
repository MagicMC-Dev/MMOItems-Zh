package net.Indyuce.mmoitems.manager;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class DamageManager implements Listener {
	private final Map<Integer, DamageInfo> customDamage = new HashMap<>();

	public boolean isDamaged(Entity entity) {
		return customDamage.containsKey(entity.getEntityId());
	}

	public void setDamaged(Entity entity, double value, DamageType type) {
		if (!customDamage.containsKey(entity.getEntityId()))
			customDamage.put(entity.getEntityId(), new DamageInfo(value, type));
	}

	public DamageInfo getDamage(Entity entity) {
		return customDamage.get(entity.getEntityId());
	}

	public void removeDamaged(Entity entity) {
		customDamage.remove(Integer.valueOf(entity.getEntityId()));
	}

	public void damage(TemporaryStats playerStats, LivingEntity target, double value, DamageType damageType) {
		damage(playerStats, target, value, damageType, true);
	}

	public void damage(TemporaryStats playerStats, LivingEntity target, double value, DamageType damageType, boolean knockback) {
		if (target.hasMetadata("NPC") || playerStats.getPlayer().hasMetadata("NPC"))
			return;

		/*
		 * calculate extra damage depending on the type of attack and the entity
		 * that is being damaged
		 */
		if (damageType.isSpell()) {
			value *= 1 + (damageType == DamageType.MAGIC ? playerStats.getStat(ItemStat.MAGIC_DAMAGE) / 100 : 0);
			value *= 1 + (isUndead(target) ? playerStats.getStat(ItemStat.UNDEAD_DAMAGE) / 100 : 0);
			value *= 1 + (playerStats.getStat(target instanceof Player ? ItemStat.PVP_DAMAGE : ItemStat.PVE_DAMAGE) / 100);
		}

		setDamaged(target, value, damageType);

		if (!knockback) {
			final double baseKnockbackValue = target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getBaseValue();
			target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(63);
			try {
				target.damage(value, playerStats.getPlayer());
			} catch (Exception e) {
				e.printStackTrace();
			}
			target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(baseKnockbackValue);
			return;
		}

		target.damage(value, playerStats.getPlayer());
	}

	public boolean isUndead(Entity entity) {
		return entity instanceof Zombie || entity instanceof Skeleton || entity instanceof Wither;
	}

	public enum DamageType {

		// damage dealt by weapons & weapon passives
		WEAPON,

		// damage dealt by physical skills
		PHYSICAL,

		// damage dealt by magical abilities
		MAGIC,

		// damage dealt by projectile based weapons
		PROJECTILE;

		public boolean isSpell() {
			return this == MAGIC || this == PHYSICAL;
		}
	}

	public class DamageInfo {
		private final double value;
		private final DamageType type;

		private DamageInfo(double value, DamageType type) {
			this.value = value;
			this.type = type;
		}

		public double getValue() {
			return value;
		}

		public DamageType getType() {
			return type;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void a(EntityDamageByEntityEvent event) {
		removeDamaged(event.getEntity());
	}
}
