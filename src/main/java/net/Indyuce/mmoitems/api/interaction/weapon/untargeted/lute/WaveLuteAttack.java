package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute;

import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.api.util.SoundReader;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.stat.data.ProjectileParticlesData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class WaveLuteAttack implements LuteAttackHandler {

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

					Vector vec = MMOUtils.rotateFunc(new Vector(.5, 0, 0), loc);

					if (nbt.hasTag("MMOITEMS_PROJECTILE_PARTICLES")) {
						JsonObject obj = MythicLib.plugin.getJson().parse(nbt.getString("MMOITEMS_PROJECTILE_PARTICLES"), JsonObject.class);
						Particle particle = Particle.valueOf(obj.get("Particle").getAsString());
						// If the selected particle is colored, use the provided color
						if (ProjectileParticlesData.isColorable(particle)) {
							double red = Double.parseDouble(String.valueOf(obj.get("Red")));
							double green = Double.parseDouble(String.valueOf(obj.get("Green")));
							double blue = Double.parseDouble(String.valueOf(obj.get("Blue")));
							ProjectileParticlesData.shootParticle(stats.getPlayer(), particle, loc.clone().add(vec.multiply(Math.sin((double) ti / 2))), red, green, blue);
							ProjectileParticlesData.shootParticle(stats.getPlayer(), particle, loc.clone().add(vec.multiply(-1)), red, green, blue);
							// If it's not colored, just shoot the particle
						} else {
							ProjectileParticlesData.shootParticle(stats.getPlayer(), particle, loc.clone().add(vec.multiply(Math.sin((double) ti / 2))), 0, 0, 0);
							ProjectileParticlesData.shootParticle(stats.getPlayer(), particle, loc.clone().add(vec.multiply(-1)), 0, 0, 0);
						}
						// If no particle has been provided via projectile particle attribute, default to this particle
					} else {
						loc.getWorld().spawnParticle(Particle.NOTE, loc.clone().add(vec.multiply(Math.sin((double) ti / 2))), 0);
						loc.getWorld().spawnParticle(Particle.NOTE, loc.clone().add(vec.multiply(-1)), 0);
					}

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

