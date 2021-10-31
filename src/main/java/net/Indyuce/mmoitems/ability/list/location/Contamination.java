package net.Indyuce.mmoitems.ability.list.location;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.LocationAbility;
import net.Indyuce.mmoitems.ability.metadata.LocationAbilityMetadata;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class Contamination extends LocationAbility {
	public Contamination() {
		super();

		addModifier("damage", 2);
		addModifier("duration", 8);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(AttackMetadata attack, LocationAbilityMetadata ability) {
		Location loc = ability.getTarget();

		double duration = Math.min(30, ability.getModifier("duration")) * 20;

		loc.add(0, .1, 0);
		new BukkitRunnable() {
			final double dps = ability.getModifier("damage") / 2;
			double ti = 0;
			int j = 0;

			public void run() {
				j++;
				if (j >= duration)
					cancel();

				loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone().add(Math.cos(ti / 3) * 5, 0, Math.sin(ti / 3) * 5), 1,
						new Particle.DustOptions(Color.PURPLE, 1));
				for (int j = 0; j < 3; j++) {
					ti += Math.PI / 32;
					double r = Math.sin(ti / 2) * 4;
					for (double k = 0; k < Math.PI * 2; k += Math.PI * 2 / 3)
						loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc.clone().add(r * Math.cos(k + ti / 4), 0, r * Math.sin(k + ti / 4)), 0);
				}

				if (j % 10 == 0) {
					loc.getWorld().playSound(loc, VersionSound.ENTITY_ENDERMAN_HURT.toSound(), 2, 1);
					for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
						if (MMOUtils.canTarget(attack.getPlayer(), entity) && entity.getLocation().distanceSquared(loc) <= 25)
							new AttackMetadata(new DamageMetadata(dps, DamageType.SKILL, DamageType.MAGIC), attack.getStats()).damage((LivingEntity) entity, false);
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
