package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;

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
	public void whenCast(CachedStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
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
				new AttackResult(damage, DamageType.SKILL, DamageType.MAGICAL).damage(stats.getPlayer(), (LivingEntity) entity);
				entity.setVelocity(MMOUtils.normalize(entity.getLocation().subtract(loc).toVector().setY(0)).setY(.2).multiply(2 * knockback));
			}
	}
}
