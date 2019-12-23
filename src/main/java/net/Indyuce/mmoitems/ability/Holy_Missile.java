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
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.version.VersionSound;

public class Holy_Missile extends Ability {
	public Holy_Missile() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 6);
		addModifier("cooldown", 10);
		addModifier("duration", 4);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(CachedStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		double duration = data.getModifier("duration") * 10;
		double damage = data.getModifier("damage");
		
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 1);
		new BukkitRunnable() {
			Vector vec = getTargetDirection(stats.getPlayer(), target).multiply(.45);
			Location loc = stats.getPlayer().getLocation().clone().add(0, 1.3, 0);
			double ti = 0;

			public void run() {
				if (ti++ > duration)
					cancel();

				loc.getWorld().playSound(loc, VersionSound.BLOCK_NOTE_BLOCK_HAT.toSound(), 2, 1);
				List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
				for (int j = 0; j < 2; j++) {
					loc.add(vec);
					if (loc.getBlock().getType().isSolid())
						cancel();

					for (double i = -Math.PI; i < Math.PI; i += Math.PI / 2) {
						Vector v = new Vector(Math.cos(i + ti / 4), Math.sin(i + ti / 4), 0);
						v = MMOUtils.rotateFunc(v, loc);
						loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 0, v.getX(), v.getY(), v.getZ(), .08);
					}

					for (Entity entity : entities)
						if (MMOUtils.canDamage(stats.getPlayer(), loc, entity)) {
							loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 1);
							loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 32, 0, 0, 0, .2);
							loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
							new ItemAttackResult(damage, DamageType.SKILL, DamageType.MAGICAL, DamageType.PROJECTILE).applyEffectsAndDamage(stats, null, (LivingEntity) entity);
							cancel();
							return;
						}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}

