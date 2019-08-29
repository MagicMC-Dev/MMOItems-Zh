package net.Indyuce.mmoitems.ability;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.AttackResult.DamageType;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Explosive_Turkey extends Ability {
	public Explosive_Turkey() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 6);
		addModifier("radius", 4);
		addModifier("knockback", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		double damage = data.getModifier("damage");
		double radiusSquared = Math.pow(data.getModifier("radius"), 2);
		double knockback = data.getModifier("knockback");

		final Chicken chicken = (Chicken) stats.getPlayer().getWorld().spawnEntity(stats.getPlayer().getLocation().add(0, 1.3, 0), EntityType.CHICKEN);
		chicken.setInvulnerable(true);

		/*
		 * when items are moving through the air, they loose a percent of their
		 * velocity proportionally to their coordinates in each axis. this means
		 * that if the trajectory is not affected, the ratio of x/y will always
		 * be the same. check for any change of that ratio to check for a
		 * trajectory change
		 */
		Vector vec = stats.getPlayer().getEyeLocation().getDirection().clone().multiply(.6);
		chicken.setVelocity(vec);
		final double trajRatio = chicken.getVelocity().getX() / chicken.getVelocity().getZ();

		new BukkitRunnable() {
			double ti = 0;

			public void run() {
				if (ti++ > 40 || chicken.isDead() || chicken == null) {
					chicken.remove();
					cancel();
					return;
				}

				if (ti % 4 == 0)
					chicken.getWorld().playSound(chicken.getLocation(), Sound.ENTITY_CHICKEN_HURT, 2, 1);
				chicken.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, chicken.getLocation().add(0, .3, 0), 0);
				chicken.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, chicken.getLocation().add(0, .3, 0), 1, 0, 0, 0, .05);
				double currentTrajRatio = chicken.getVelocity().getX() / chicken.getVelocity().getZ();
				if (chicken.isOnGround() || Math.abs(trajRatio - currentTrajRatio) > .1) {
					chicken.remove();
					chicken.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, chicken.getLocation().add(0, .3, 0), 128, 0, 0, 0, .25);
					chicken.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, chicken.getLocation().add(0, .3, 0), 24, 0, 0, 0, .25);
					chicken.getWorld().playSound(chicken.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 1.5f);
					for (Entity entity : MMOUtils.getNearbyChunkEntities(chicken.getLocation()))
						if (!entity.isDead() && entity.getLocation().distanceSquared(chicken.getLocation()) < radiusSquared && MMOUtils.canDamage(stats.getPlayer(), entity)) {
							new AttackResult(damage, DamageType.SKILL, DamageType.MAGICAL, DamageType.PROJECTILE).applyEffectsAndDamage(stats, null, (LivingEntity) entity);
							entity.setVelocity(entity.getLocation().toVector().subtract(chicken.getLocation().toVector()).multiply(.1 * knockback).setY(.4 * knockback));
						}
					cancel();
					return;
				}
				chicken.setVelocity(vec);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
