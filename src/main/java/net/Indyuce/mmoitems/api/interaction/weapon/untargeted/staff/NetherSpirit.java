package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

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
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.AttackResult.DamageType;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;

public class NetherSpirit implements StaffAttackHandler {

	@Override
	public void handle(TemporaryStats stats, NBTItem nbt, double attackDamage, double range) {
		new BukkitRunnable() {
			Vector vec = stats.getPlayer().getEyeLocation().getDirection().multiply(.3);
			Location loc = stats.getPlayer().getEyeLocation();
			int ti = 0;

			public void run() {
				ti++;
				if (ti % 2 == 0)
					loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 2, 2);
				List<Entity> targets = MMOUtils.getNearbyChunkEntities(loc);
				for (int j = 0; j < 3; j++) {
					loc.add(vec);
					if (loc.getBlock().getType().isSolid()) {
						cancel();
						break;
					}

					loc.getWorld().spawnParticle(Particle.FLAME, loc, 2, .07, .07, .07, 0);
					loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 0);
					for (Entity target : targets)
						if (MMOUtils.canDamage(stats.getPlayer(), loc, target)) {
							new AttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.MAGICAL).applyEffectsAndDamage(stats, nbt, (LivingEntity) target);
							loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
							cancel();
							return;
						}
				}
				if (ti >= range)
					cancel();
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
