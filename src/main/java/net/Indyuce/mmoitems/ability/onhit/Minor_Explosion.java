package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.AttackResult.DamageType;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Minor_Explosion extends Ability {
	public Minor_Explosion() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT);

		addModifier("damage", 6);
		addModifier("knockback", 1);
		addModifier("radius", 5);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		Location loc = getTargetLocation(stats.getPlayer(), target);
		if (loc == null) {
			result.setSuccessful(false);
			return;
		}

		double damage = data.getModifier("damage");
		double radiusSquared = Math.pow(data.getModifier("radius"), 2);
		double knockback = data.getModifier("knockback");

		loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.add(0, .1, 0), 32, 1.7, 1.7, 1.7, 0);
		loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 64, 0, 0, 0, .3);
		loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);

		for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
			if (entity.getLocation().distanceSquared(loc) < radiusSquared && MMOUtils.canDamage(stats.getPlayer(), entity)) {
				new AttackResult(damage, DamageType.SKILL, DamageType.MAGICAL).applyEffectsAndDamage(stats, null, (LivingEntity) entity);
				entity.setVelocity(normalizeIfNotNull(entity.getLocation().subtract(loc).toVector().setY(0)).setY(.2).multiply(2 * knockback));
			}
	}

	/*
	 * if the vector is null, you can't normalize it because you cannot divide
	 * by 0.
	 */
	private Vector normalizeIfNotNull(Vector vector) {
		return vector.length() == 0 ? vector : vector.normalize();
	}
}
