package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute;

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

public class WaveLuteAttack implements LuteAttackHandler {

	@Override
	public void handle(TemporaryStats stats, NBTItem nbt, double attackDamage, double range, Vector weight, Sound sound) {
		new BukkitRunnable() {
			Vector vec = stats.getPlayer().getEyeLocation().getDirection().multiply(.4);
			Location loc = stats.getPlayer().getEyeLocation();
			int ti = 0;

			public void run() {
				if (ti++ > range)
					cancel();

				List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
				for (int j = 0; j < 3; j++) {
					loc.add(vec.add(weight));
					if (loc.getBlock().getType().isSolid()) {
						cancel();
						break;
					}

					Vector vec = MMOUtils.rotateFunc(new Vector(.5, 0, 0), loc);
					loc.getWorld().spawnParticle(Particle.NOTE, loc.clone().add(vec.multiply(Math.sin((double) ti / 2))), 0);
					loc.getWorld().spawnParticle(Particle.NOTE, loc.clone().add(vec.multiply(-1)), 0);
					if (j == 0)
						loc.getWorld().playSound(loc, sound, 2, (float) (.5 + (double) ti / range));

					for (Entity target : entities)
						if (MMOUtils.canDamage(stats.getPlayer(), loc, target)) {
							new AttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE).applyEffectsAndDamage(stats, nbt, (LivingEntity) target);
							cancel();
							return;
						}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}

