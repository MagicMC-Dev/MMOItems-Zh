package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.VersionSound;

public class Freeze extends Ability {
	public Freeze() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT);

		addModifier("duration", 4);
		addModifier("amplifier", 2);
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

		int duration = (int) (data.getModifier("duration") * 20);
		int amplifier = (int) (data.getModifier("amplifier") - 1);
		double radiusSquared = Math.pow(data.getModifier("radius"), 2);

		loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.add(0, .1, 0), 0);
		loc.getWorld().spawnParticle(Particle.SNOW_SHOVEL, loc, 48, 0, 0, 0, .2);
		loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 24, 0, 0, 0, .2);
		loc.getWorld().playSound(loc, VersionSound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.toSound(), 2, 1);

		for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
			if (entity.getLocation().distanceSquared(loc) < radiusSquared && MMOUtils.canDamage(stats.getPlayer(), entity)) {
				((LivingEntity) entity).removePotionEffect(PotionEffectType.SLOW);
				((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, amplifier));
			}
	}
}
