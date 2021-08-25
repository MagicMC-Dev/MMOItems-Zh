package net.Indyuce.mmoitems.ability.list.location;

import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.LocationAbility;
import net.Indyuce.mmoitems.ability.metadata.LocationAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Freeze extends LocationAbility {
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
	public LocationAbilityMetadata canBeCast(ItemAttackMetadata attack, LivingEntity target, AbilityData ability) {
		return new LocationAbilityMetadata(ability, attack.getDamager(), target);
	}

	@Override
	public void whenCast(ItemAttackMetadata attack, LocationAbilityMetadata ability) {
		Location loc = ability.getTarget();

		int duration = (int) (ability.getModifier("duration") * 20);
		int amplifier = (int) (ability.getModifier("amplifier") - 1);
		double radiusSquared = Math.pow(ability.getModifier("radius"), 2);

		loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.add(0, .1, 0), 0);
		loc.getWorld().spawnParticle(Particle.SNOW_SHOVEL, loc, 48, 0, 0, 0, .2);
		loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 24, 0, 0, 0, .2);
		loc.getWorld().playSound(loc, VersionSound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.toSound(), 2, 1);

		for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
			if (entity.getLocation().distanceSquared(loc) < radiusSquared && MMOUtils.canTarget(attack.getDamager(), entity)) {
				((LivingEntity) entity).removePotionEffect(PotionEffectType.SLOW);
				((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, amplifier));
			}
	}
}
