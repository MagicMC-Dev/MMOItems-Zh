package net.Indyuce.mmoitems.ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
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

public class Tactical_Grenade extends Ability {
	public Tactical_Grenade() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
		addModifier("knock-up", 1);
		addModifier("damage", 4);
		addModifier("radius", 4);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity initialTarget, AbilityData data, AttackResult result) {
		LivingEntity target = initialTarget == null ? MMOItems.plugin.getVersion().getVersionWrapper().rayTrace(stats.getPlayer(), 50).getHit() : initialTarget;
		if (target == null) {
			result.setSuccessful(false);
			return;
		}

		new BukkitRunnable() {
			int j = 0;
			Location loc = stats.getPlayer().getLocation().add(0, .1, 0);
			double radius = data.getModifier("radius");
			double knockup = .7 * data.getModifier("knock-up");
			List<Integer> hit = new ArrayList<>();

			public void run() {
				j++;
				if (target.isDead() || !target.getWorld().equals(loc.getWorld()) || j > 200) {
					cancel();
					return;
				}

				Vector vec = target.getLocation().add(0, .1, 0).subtract(loc).toVector();
				vec = vec.length() < 3 ? vec : vec.normalize().multiply(3);
				loc.add(vec);

				loc.getWorld().spawnParticle(Particle.CLOUD, loc, 32, 1, 0, 1, 0);
				loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 16, 1, 0, 1, .05);
				loc.getWorld().playSound(loc, Sound.BLOCK_ANVIL_LAND, 2, 0);
				loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);

				for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
					if (!hit.contains(entity.getEntityId()) && MMOUtils.canDamage(stats.getPlayer(), entity) && entity.getLocation().distanceSquared(loc) < radius * radius) {

						/*
						 * stop the runnable as soon as the grenade finally hits
						 * the initial target.
						 */
						hit.add(entity.getEntityId());
						if (entity.equals(target))
							cancel();

						new AttackResult(data.getModifier("damage"), DamageType.SKILL, DamageType.MAGICAL).applyEffectsAndDamage(stats, null, (LivingEntity) entity);
						entity.setVelocity(entity.getVelocity().add(offsetVector(knockup)));
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 12);
	}

	private Vector offsetVector(double y) {
		return new Vector(2 * (random.nextDouble() - .5), y, 2 * (random.nextDouble() - .5));
	}
}