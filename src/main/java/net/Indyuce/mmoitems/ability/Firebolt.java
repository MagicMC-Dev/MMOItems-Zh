package net.Indyuce.mmoitems.ability;

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
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.version.VersionSound;

public class Firebolt extends Ability {
	public Firebolt() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 6);
		addModifier("ignite", 3);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 1);
		new BukkitRunnable() {
			Vector vec = getTargetDirection(stats.getPlayer(), target).multiply(.8);
			Location loc = stats.getPlayer().getEyeLocation();
			int ti = 0;

			public void run() {
				ti++;
				if (ti > 20)
					cancel();

				List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
				loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 2, 1);
				for (int j = 0; j < 2; j++) {
					loc.add(vec);
					if (loc.getBlock().getType().isSolid())
						cancel();

					loc.getWorld().spawnParticle(Particle.FLAME, loc, 5, .12, .12, .12, 0);
					if (random.nextDouble() < .3)
						loc.getWorld().spawnParticle(Particle.LAVA, loc, 0);
					for (Entity target : entities)
						if (MMOUtils.canDamage(stats.getPlayer(), loc, target)) {
							loc.getWorld().spawnParticle(Particle.FLAME, loc, 32, 0, 0, 0, .1);
							loc.getWorld().spawnParticle(Particle.LAVA, loc, 8, 0, 0, 0, 0);
							loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
							loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 3, 1);
							new ItemAttackResult(data.getModifier("damage"), DamageType.SKILL, DamageType.MAGICAL, DamageType.PROJECTILE).applyEffectsAndDamage(stats, null, (LivingEntity) target);
							target.setFireTicks((int) data.getModifier("ignite") * 20);
							cancel();
							return;
						}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
