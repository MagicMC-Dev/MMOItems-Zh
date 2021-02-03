package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import org.bukkit.Location;
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
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionSound;

public class ThunderSpirit implements StaffAttackHandler {

	@Override
	public void handle(CachedStats stats, NBTItem nbt, double attackDamage, double range) {
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
		new BukkitRunnable() {
			final Location target = getGround(stats.getPlayer().getTargetBlock(null, (int) range * 2).getLocation()).add(0, 1.2, 0);
			final double a = random.nextDouble() * Math.PI * 2;
			final Location loc = target.clone().add(Math.cos(a) * 4, 10, Math.sin(a) * 4);
			final Vector vec = target.toVector().subtract(loc.toVector()).multiply(.015);
			double ti = 0;

			public void run() {
				loc.getWorld().playSound(loc, VersionSound.BLOCK_NOTE_BLOCK_HAT.toSound(), 2, 2);
				for (int j = 0; j < 4; j++) {
					ti += .015;
					loc.add(vec);
					loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 0, .03, 0, .03, 0);
					if (ti >= 1) {
						loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 24, 0, 0, 0, .12);
						loc.getWorld().playSound(loc, VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 2, 2);
						for (Entity target : MMOUtils.getNearbyChunkEntities(loc))
							if (MMOUtils.canDamage(stats.getPlayer(), target) && target.getLocation().distanceSquared(loc) <= 9)
								new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.MAGIC).applyEffectsAndDamage(stats, nbt, (LivingEntity) target);
						cancel();
					}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
