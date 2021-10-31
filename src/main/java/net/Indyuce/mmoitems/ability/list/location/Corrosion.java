package net.Indyuce.mmoitems.ability.list.location;

import io.lumine.mythic.lib.damage.AttackMetadata;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.LocationAbility;
import net.Indyuce.mmoitems.ability.metadata.LocationAbilityMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Corrosion extends LocationAbility {
	public Corrosion() {
		super();

		addModifier("duration", 4);
		addModifier("amplifier", 1);
		addModifier("radius", 5);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(AttackMetadata attack, LocationAbilityMetadata ability) {
		Location loc = ability.getTarget();

		int duration = (int) (ability.getModifier("duration") * 20);
		int amplifier = (int) ability.getModifier("amplifier");
		double radiusSquared = Math.pow(ability.getModifier("radius"), 2);

		loc.getWorld().spawnParticle(Particle.SLIME, loc, 48, 2, 2, 2, 0);
		loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 32, 2, 2, 2, 0);
		loc.getWorld().playSound(loc, Sound.BLOCK_BREWING_STAND_BREW, 2, 0);

		for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
			if (entity.getLocation().distanceSquared(loc) < radiusSquared && MMOUtils.canTarget(attack.getPlayer(), entity)) {
				((LivingEntity) entity).removePotionEffect(PotionEffectType.POISON);
				((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration, amplifier));
			}
	}
}
