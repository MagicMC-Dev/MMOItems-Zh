package net.Indyuce.mmoitems.ability.list.location;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.LocationAbility;
import net.Indyuce.mmoitems.ability.metadata.LocationAbilityMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Arcane_Hail extends LocationAbility {
	public Arcane_Hail() {
		super();

		addModifier("damage", 3);
		addModifier("duration", 4);
		addModifier("radius", 3);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(AttackMetadata attack, LocationAbilityMetadata ability) {
		Location loc = ability.getTarget();

		double damage = ability.getModifier("damage");
		double duration = ability.getModifier("duration") * 10;
		double radius = ability.getModifier("radius");

		new BukkitRunnable() {
			int j = 0;

			public void run() {
				j++;
				if (j > duration) {
					cancel();
					return;
				}

				Location loc1 = loc.clone().add(randomCoordMultiplier() * radius, 0, randomCoordMultiplier() * radius);
				loc1.getWorld().playSound(loc1, VersionSound.ENTITY_ENDERMAN_HURT.toSound(), 1, 0);
				for (Entity entity : MMOUtils.getNearbyChunkEntities(loc1))
					if (MMOUtils.canTarget(attack.getPlayer(), entity) && entity.getLocation().distanceSquared(loc1) <= 4)
						new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC), attack.getStats()).damage((LivingEntity) entity);
				loc1.getWorld().spawnParticle(Particle.SPELL_WITCH, loc1, 12, 0, 0, 0, .1);
				loc1.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc1, 6, 0, 0, 0, .1);

				Vector vector = new Vector(randomCoordMultiplier() * .03, .3, randomCoordMultiplier() * .03);
				for (double k = 0; k < 60; k++)
					loc1.getWorld().spawnParticle(Particle.SPELL_WITCH, loc1.add(vector), 0);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}

	// random double between -1 and 1
	private double randomCoordMultiplier() {
		return (random.nextDouble() - .5) * 2;
	}
}
