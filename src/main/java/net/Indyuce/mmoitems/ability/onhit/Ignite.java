package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.version.VersionSound;

public class Ignite extends Ability {
	public Ignite() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT);

		addModifier("duration", 80);
		addModifier("max-ignite", 200);
		addModifier("radius", 5);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		Location loc = getTargetLocation(stats.getPlayer(), target);
		if (loc == null) {
			result.setSuccessful(false);
			return;
		}

		int maxIgnite = (int) (data.getModifier("max-ignite") * 20);
		int ignite = (int) (data.getModifier("duration") * 20);
		double radiusSquared = Math.pow(data.getModifier("radius"), 2);

		loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.add(0, .1, 0), 0);
		loc.getWorld().spawnParticle(Particle.LAVA, loc, 12);
		loc.getWorld().spawnParticle(Particle.FLAME, loc, 48, 0, 0, 0.13);
		loc.getWorld().playSound(loc, VersionSound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.toSound(), 2, 1);

		for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
			if (entity.getLocation().distanceSquared(loc) < radiusSquared && MMOUtils.canDamage(stats.getPlayer(), entity))
				entity.setFireTicks(Math.min(entity.getFireTicks() + ignite, maxIgnite));
	}
}
