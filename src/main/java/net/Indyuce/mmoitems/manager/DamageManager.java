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

import net.Indyuce.mmoitems.api.AttackResult;

public class DamageManager implements Listener {
	private final Map<Integer, AttackResult> customDamage = new HashMap<>();

	public boolean isDamaged(Entity entity) {
		return customDamage.containsKey(entity.getEntityId());
	}

	public AttackResult getDamage(Entity entity) {
		return customDamage.get(entity.getEntityId());
	}

	public void damage(Player player, LivingEntity target, AttackResult result) {
		damage(player, target, result, true);
	}

	public void damage(Player player, LivingEntity target, AttackResult result, boolean knockback) {
		if (target.hasMetadata("NPC") || player.hasMetadata("NPC"))
			return;

		/*
		 * calculate extra damage depending on the type of attack and the entity
		 * that is being damaged
		 */
		customDamage.put(target.getEntityId(), result);

		if (!knockback) {
			final double baseKnockbackValue = target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).getBaseValue();
			target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(63);
			try {
				target.damage(result.getDamage(), player);
			} catch (Exception e) {
				e.printStackTrace();
			}
			target.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(baseKnockbackValue);
			
		} else
			target.damage(result.getDamage(), player);
	}

	public boolean isUndead(Entity entity) {
		return entity instanceof Zombie || entity instanceof Skeleton || entity instanceof Wither;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void a(EntityDamageByEntityEvent event) {
		customDamage.remove(Integer.valueOf(event.getEntity().getEntityId()));
	}
}
