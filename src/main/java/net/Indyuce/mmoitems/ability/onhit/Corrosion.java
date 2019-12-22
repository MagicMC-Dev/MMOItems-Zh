package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.player.damage.AttackResult;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Corrosion extends Ability {
	public Corrosion() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT);

		addModifier("duration", 4);
		addModifier("amplifier", 1);
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
		int amplifier = (int) data.getModifier("amplifier");
		double radiusSquared = Math.pow(data.getModifier("radius"), 2);

		loc.getWorld().spawnParticle(Particle.SLIME, loc, 48, 2, 2, 2, 0);
		loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 32, 2, 2, 2, 0);
		loc.getWorld().playSound(loc, Sound.BLOCK_BREWING_STAND_BREW, 2, 0);

		for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
			if (entity.getLocation().distanceSquared(loc) < radiusSquared && MMOUtils.canDamage(stats.getPlayer(), entity)) {
				((LivingEntity) entity).removePotionEffect(PotionEffectType.POISON);
				((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, amplifier));
			}
	}
}
