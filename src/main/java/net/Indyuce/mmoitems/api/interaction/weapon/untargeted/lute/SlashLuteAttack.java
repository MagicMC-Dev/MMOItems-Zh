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

public class SlashLuteAttack implements LuteAttackHandler {

	@Override
	public void handle(CachedStats stats, NBTItem nbt, double attackDamage, double range, Vector weight, SoundReader sound) {
		new BukkitRunnable() {
			final Vector vec = stats.getPlayer().getEyeLocation().getDirection();
			final Location loc = stats.getPlayer().getLocation().add(0, 1.3, 0);
			double ti = 1;

			public void run() {
				if ((ti += .6) > 5) cancel();

				sound.play(loc, 2, (float) (.5 + ti / range));
				for (int k = -30; k < 30; k += 3)
					if (random.nextBoolean()) {
						loc.setDirection(vec);
						loc.setYaw(loc.getYaw() + k);
						loc.setPitch(stats.getPlayer().getEyeLocation().getPitch());
						loc.getWorld().spawnParticle(Particle.NOTE, loc.clone().add(loc.getDirection().multiply(1.5 * ti)), 0);
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);

		for (Entity entity : MMOUtils.getNearbyChunkEntities(stats.getPlayer().getLocation()))
			if (entity.getLocation().distanceSquared(stats.getPlayer().getLocation()) < 40 && stats.getPlayer().getEyeLocation().getDirection().angle(entity.getLocation().toVector().subtract(stats.getPlayer().getLocation().toVector())) < Math.PI / 6 && MMOUtils.canDamage(stats.getPlayer(), entity))
				new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE).applyEffectsAndDamage(stats, nbt, (LivingEntity) entity);
	}
}
