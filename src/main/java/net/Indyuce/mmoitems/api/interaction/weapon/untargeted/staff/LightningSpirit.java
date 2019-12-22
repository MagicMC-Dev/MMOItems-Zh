package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionSound;

public class LightningSpirit implements StaffAttackHandler {

	@Override
	public void handle(TemporaryStats stats, NBTItem nbt, double attackDamage, double range) {
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 2, 2);
		Location loc = stats.getPlayer().getEyeLocation();
		Vector vec = stats.getPlayer().getEyeLocation().getDirection().multiply(.75);
		for (int j = 0; j < range; j++) {
			loc.add(vec);
			if (loc.getBlock().getType().isSolid())
				break;

			loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 0);
			for (Entity target : MMOUtils.getNearbyChunkEntities(loc))
				if (MMOUtils.canDamage(stats.getPlayer(), loc, target)) {
					new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.MAGICAL).applyEffectsAndDamage(stats, nbt, (LivingEntity) target);
					loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 16, 0, 0, 0, .1);
					return;
				}
		}
	}
}
