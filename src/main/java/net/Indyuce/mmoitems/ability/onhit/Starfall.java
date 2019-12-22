package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.version.VersionSound;

public class Starfall extends Ability {
	public Starfall() {
		super(CastingMode.ON_HIT);

		addModifier("cooldown", 8);
		addModifier("damage", 3.5);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		if (target == null) {
			result.setSuccessful(false);
			return;
		}

		new BukkitRunnable() {
			double ran = random.nextDouble() * Math.PI * 2;
			Location loc = target.getLocation().add(Math.cos(ran) * 3, 6, Math.sin(ran) * 3);
			Vector vec = target.getLocation().add(0, .65, 0).toVector().subtract(loc.toVector()).multiply(.05);
			double ti = 0;

			public void run() {
				loc.getWorld().playSound(loc, VersionSound.BLOCK_NOTE_BLOCK_HAT.toSound(), 2, 2);
				for (int j = 0; j < 2; j++) {
					ti += .05;

					loc.add(vec);
					loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 1, .04, 0, .04, 0);
					if (ti >= 1) {
						loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 24, 0, 0, 0, .12);
						loc.getWorld().playSound(loc, VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 2);
						cancel();
					}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
		target.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
		result.addDamage(data.getModifier("damage"));
	}
}
