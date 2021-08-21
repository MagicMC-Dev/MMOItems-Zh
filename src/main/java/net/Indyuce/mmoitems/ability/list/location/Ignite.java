package net.Indyuce.mmoitems.ability.list.location;

import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.LocationAbility;
import net.Indyuce.mmoitems.ability.metadata.LocationAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;

public class Ignite extends LocationAbility {
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
	public void whenCast(ItemAttackMetadata attack, LocationAbilityMetadata ability) {
		Location loc = ability.getTarget();

		int maxIgnite = (int) (ability.getModifier("max-ignite") * 20);
		int ignite = (int) (ability.getModifier("duration") * 20);
		double radiusSquared = Math.pow(ability.getModifier("radius"), 2);

		loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc.add(0, .1, 0), 0);
		loc.getWorld().spawnParticle(Particle.LAVA, loc, 12);
		loc.getWorld().spawnParticle(Particle.FLAME, loc, 48, 0, 0, 0.13);
		loc.getWorld().playSound(loc, VersionSound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST.toSound(), 2, 1);

		for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
			if (entity.getLocation().distanceSquared(loc) < radiusSquared && MMOUtils.canDamage(attack.getDamager(), entity))
				entity.setFireTicks(Math.min(entity.getFireTicks() + ignite, maxIgnite));
	}
}
