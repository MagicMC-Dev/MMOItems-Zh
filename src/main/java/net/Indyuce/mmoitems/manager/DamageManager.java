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

import net.Indyuce.mmoitems.api.DamageInfo;
import net.Indyuce.mmoitems.api.DamageInfo.DamageType;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class DamageManager implements Listener {
	private final Map<Integer, DamageInfo> customDamage = new HashMap<>();

	public boolean isDamaged(Entity entity) {
		return customDamage.containsKey(entity.getEntityId());
	}

	public DamageInfo getDamage(Entity entity) {
		return customDamage.get(entity.getEntityId());
	}

	public void damage(TemporaryStats playerStats, LivingEntity target, double value, DamageType... types) {
		damage(playerStats, target, value, true, types);
	}

	public void damage(TemporaryStats playerStats, LivingEntity target, double value, boolean knockback, DamageType... types) {
		if (target.hasMetadata("NPC") || playerStats.getPlayer().hasMetadata("NPC"))
			return;

		/*
		 * calculate extra damage depending on the type of attack and the entity
		 * that is being damaged
		 */
		DamageInfo info = new DamageInfo(value, types);
		if (info.hasType(DamageType.SKILL)) {
			if (info.hasType(DamageType.MAGICAL))
				value *= 1 + playerStats.getStat(ItemStat.MAGIC_DAMAGE) / 100;
			if (isUndead(target))
				value *= 1 + playerStats.getStat(ItemStat.UNDEAD_DAMAGE) / 100;
			value *= 1 + (playerStats.getStat(target instanceof Player ? ItemStat.PVP_DAMAGE : ItemStat.PVE_DAMAGE) / 100);
		}

		customDamage.put(target.getEntityId(), info);

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

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void a(EntityDamageByEntityEvent event) {
		customDamage.remove(Integer.valueOf(event.getEntity().getEntityId()));
	}
}
