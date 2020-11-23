package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.VectorAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;

public class Bouncy_Fireball extends Ability {
	public Bouncy_Fireball() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 20);
		addModifier("damage", 5);
		addModifier("ignite", 40);
		addModifier("speed", 1);
		addModifier("radius", 4);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new VectorAbilityResult(ability, stats.getPlayer(), target);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_SNOWBALL_THROW, 2, 0);
		new BukkitRunnable() {
			int j = 0;
			final Vector vec = ((VectorAbilityResult) ability).getTarget().setY(0).normalize().multiply(.5 * ability.getModifier("speed"));
			final Location loc = stats.getPlayer().getLocation().clone().add(0, 1.2, 0);
			int bounces = 0;

			double y = .3;

			public void run() {
				j++;
				if (j > 100) {
					loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 32, 0, 0, 0, .05);
					loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);
					cancel();
					return;
				}

				loc.add(vec);
				loc.add(0, y, 0);
				if (y > -.6)
					y -= .05;

				loc.getWorld().spawnParticle(Particle.LAVA, loc, 0);
				loc.getWorld().spawnParticle(Particle.FLAME, loc, 4, 0, 0, 0, .03);
				loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 1, 0, 0, 0, .03);

				if (loc.getBlock().getType().isSolid()) {
					loc.add(0, -y, 0);
					loc.add(vec.clone().multiply(-1));
					y = .4;
					bounces++;
					loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_HURT, 3, 2);
				}

				if (bounces > 2) {
					double radius = ability.getModifier("radius");
					double damage = ability.getModifier("damage");
					double ignite = ability.getModifier("ignite");

					for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
						if (entity.getLocation().distanceSquared(loc) < radius * radius)
							if (MMOUtils.canDamage(stats.getPlayer(), entity)) {
								new AttackResult(damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE).damage(stats.getPlayer(), (LivingEntity) entity);
								entity.setFireTicks((int) (ignite * 20));
							}

					loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 12, 2, 2, 2, 0);
					loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 48, 0, 0, 0, .2);
					loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 3, 0);
					cancel();
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
