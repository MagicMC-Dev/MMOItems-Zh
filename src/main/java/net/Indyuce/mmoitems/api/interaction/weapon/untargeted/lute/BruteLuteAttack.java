package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.api.util.SoundReader;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.item.NBTItem;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class BruteLuteAttack implements LuteAttackHandler {

	@Override
	public void handle(CachedStats stats, NBTItem nbt, double attackDamage, double range, Vector weight, SoundReader sound) {
		new BukkitRunnable() {
			final Vector vec = stats.getPlayer().getEyeLocation().getDirection().multiply(.4);
			final Location loc = stats.getPlayer().getEyeLocation();
			int ti = 0;

			public void run() {
				if (ti++ > range) cancel();

				List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
				for (int j = 0; j < 3; j++) {
					loc.add(vec.add(weight));
					if (loc.getBlock().getType().isSolid()) {
						cancel();
						break;
					}

					loc.getWorld().spawnParticle(Particle.NOTE, loc, 2, .1, .1, .1, 0);
					if (j == 0) sound.play(loc, 2, (float) (.5 + (double) ti / range));

					for (Entity target : entities)
						if (MMOUtils.canDamage(stats.getPlayer(), loc, target)) {
							new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE).applyEffectsAndDamage(stats, nbt, (LivingEntity) target);
							cancel();
							return;
						}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}

