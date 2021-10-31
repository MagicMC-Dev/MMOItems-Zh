package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute;

import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.target.InteractionType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.api.util.SoundReader;
import net.Indyuce.mmoitems.stat.data.ProjectileParticlesData;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class SlashLuteAttack implements LuteAttackHandler {

	@Override
	public void handle(ItemAttackMetadata attack, NBTItem nbt, double range, Vector weight, SoundReader sound) {
		new BukkitRunnable() {
			final Vector vec = attack.getPlayer().getEyeLocation().getDirection();
			final Location loc = attack.getPlayer().getLocation().add(0, 1.3, 0);
			double ti = 1;

			public void run() {
				if ((ti += .6) > 5) cancel();

				sound.play(loc, 2, (float) (.5 + ti / range));
				for (int k = -30; k < 30; k += 3)
					if (random.nextBoolean()) {
						loc.setDirection(vec);
						loc.setYaw(loc.getYaw() + k);
						loc.setPitch(attack.getPlayer().getEyeLocation().getPitch());

						if (nbt.hasTag("MMOITEMS_PROJECTILE_PARTICLES")) {
							JsonObject obj = MythicLib.plugin.getJson().parse(nbt.getString("MMOITEMS_PROJECTILE_PARTICLES"), JsonObject.class);
							Particle particle = Particle.valueOf(obj.get("Particle").getAsString());
							// If the selected particle is colored, use the provided color
							if (ProjectileParticlesData.isColorable(particle)) {
								double red = Double.parseDouble(String.valueOf(obj.get("Red")));
								double green = Double.parseDouble(String.valueOf(obj.get("Green")));
								double blue = Double.parseDouble(String.valueOf(obj.get("Blue")));
								ProjectileParticlesData.shootParticle(attack.getPlayer(), particle, loc.clone().add(loc.getDirection().multiply(1.5 * ti)), red, green, blue);
								// If it's not colored, just shoot the particle
							} else {
								ProjectileParticlesData.shootParticle(attack.getPlayer(), particle, loc.clone().add(loc.getDirection().multiply(1.5 * ti)), 0, 0, 0);
							}
							// If no particle has been provided via projectile particle attribute, default to this particle
						} else {
							loc.getWorld().spawnParticle(Particle.NOTE, loc.clone().add(loc.getDirection().multiply(1.5 * ti)), 0);
						}
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);

		for (Entity entity : MMOUtils.getNearbyChunkEntities(attack.getPlayer().getLocation()))
			if (entity.getLocation().distanceSquared(attack.getPlayer().getLocation()) < 40 && attack.getPlayer().getEyeLocation().getDirection().angle(entity.getLocation().toVector().subtract(attack.getPlayer().getLocation().toVector())) < Math.PI / 6 && MMOUtils.canTarget(attack.getPlayer(), entity, InteractionType.OFFENSE_ACTION))
				attack.clone().applyEffectsAndDamage(nbt, (LivingEntity) entity);
	}
}
