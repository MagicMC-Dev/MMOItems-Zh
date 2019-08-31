package net.Indyuce.mmoitems.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.comp.rpg.damage.DamageHandler;
import net.Indyuce.mmoitems.comp.rpg.damage.DamageInfo;

public class DamageManager implements Listener, DamageHandler {
	private final Map<Integer, DamageInfo> customDamage = new HashMap<>();
	private final List<DamageHandler> handlers = new ArrayList<>();

	public DamageManager() {
		handlers.add(this);
	}

	public void registerHandler(DamageHandler handler) {
		handlers.add(handler);
	}

	public boolean isDamaged(Entity entity) {
		return customDamage.containsKey(entity.getEntityId());
	}

	@Override
	public boolean hasDamage(Entity entity) {
		return customDamage.containsKey(entity.getEntityId());
	}

	public DamageInfo getDamage(Entity entity) {
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
		customDamage.put(target.getEntityId(), result.toDamageInfo());

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

	public DamageInfo findInfo(Entity entity) {
		for (DamageHandler handler : handlers)
			if (handler.hasDamage(entity))
				return handler.getDamage(entity);
		return null;
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void a(EntityDamageByEntityEvent event) {
		customDamage.remove(Integer.valueOf(event.getEntity().getEntityId()));
	}
}
