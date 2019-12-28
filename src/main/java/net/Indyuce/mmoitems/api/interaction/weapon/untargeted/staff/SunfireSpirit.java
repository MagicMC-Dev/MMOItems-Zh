package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionSound;

public class SunfireSpirit implements StaffAttackHandler {

	@Override
	public void handle(CachedStats stats, NBTItem nbt, double attackDamage, double range) {
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
		new BukkitRunnable() {
			Location target = getGround(stats.getPlayer().getTargetBlock((Set<Material>) null, (int) range * 2).getLocation()).add(0, 1.2, 0);
			double a = random.nextDouble() * Math.PI * 2;
			Location loc = target.clone().add(Math.cos(a) * 4, 10, Math.sin(a) * 4);
			Vector vec = target.toVector().subtract(loc.toVector()).multiply(.015);
			double ti = 0;

			public void run() {
				loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 2, 2);
				for (int j = 0; j < 4; j++) {
					ti += .015;
					loc.add(vec);
					loc.getWorld().spawnParticle(Particle.FLAME, loc, 0, .03, 0, .03, 0);
					if (ti >= 1) {
						loc.getWorld().spawnParticle(Particle.FLAME, loc, 24, 0, 0, 0, .12);
						loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 24, 0, 0, 0, .12);
						loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
						loc.getWorld().playSound(loc, VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 2, 2);
						for (Entity target : MMOUtils.getNearbyChunkEntities(loc))
							if (MMOUtils.canDamage(stats.getPlayer(), target) && target.getLocation().distanceSquared(loc) <= 9)
								new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.MAGIC).applyEffectsAndDamage(stats, nbt, (LivingEntity) target);
						cancel();
						break;
					}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
