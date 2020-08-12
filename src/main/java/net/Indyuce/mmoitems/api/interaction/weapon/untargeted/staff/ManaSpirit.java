package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import java.util.List;

import org.bukkit.Color;
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
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.item.NBTItem;

public class ManaSpirit implements StaffAttackHandler {

	@Override
	public void handle(CachedStats stats, NBTItem nbt, double attackDamage, double range) {
		new BukkitRunnable() {
			Vector vec = stats.getPlayer().getEyeLocation().getDirection().multiply(.4);
			Location loc = stats.getPlayer().getEyeLocation();
			int ti = 0;
			double r = .2;

			public void run() {
				if (ti++ > range)
					cancel();

				if (ti % 2 == 0)
					loc.getWorld().playSound(loc, Sound.BLOCK_SNOW_BREAK, 2, 2);
				List<Entity> targets = MMOUtils.getNearbyChunkEntities(loc);
				for (int j = 0; j < 3; j++) {
					loc.add(vec);
					if (loc.getBlock().getType().isSolid()) {
						cancel();
						break;
					}

					for (double item = 0; item < Math.PI * 2; item += Math.PI / 3.5) {
						Vector vec = MMOUtils.rotateFunc(new Vector(r * Math.cos(item), r * Math.sin(item), 0), loc);
						if (random.nextDouble() <= .6)
							loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone().add(vec), 1, new Particle.DustOptions(Color.AQUA, 1));
					}
					for (Entity target : targets)
						if (MMOUtils.canDamage(stats.getPlayer(), loc, target)) {
							new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.MAGIC).applyEffectsAndDamage(stats, nbt,
									(LivingEntity) target);
							loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
							cancel();
							return;
						}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
