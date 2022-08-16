package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute;

import com.google.gson.JsonObject;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
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
    public void handle(PlayerMetadata caster, double damage, NBTItem nbt, double range, Vector weight, SoundReader sound) {
        new BukkitRunnable() {
            final Vector vec = caster.getPlayer().getEyeLocation().getDirection();
            final Location loc = caster.getPlayer().getLocation().add(0, 1.3, 0);
            double ti = 1;

            public void run() {
                if ((ti += .6) > 5) cancel();

                sound.play(loc, 2, (float) (.5 + ti / range));
                for (int k = -30; k < 30; k += 3)
                    if (random.nextBoolean()) {
                        loc.setDirection(vec);
                        loc.setYaw(loc.getYaw() + k);
                        loc.setPitch(caster.getPlayer().getEyeLocation().getPitch());

                        if (nbt.hasTag("MMOITEMS_PROJECTILE_PARTICLES")) {
                            JsonObject obj = MythicLib.plugin.getJson().parse(nbt.getString("MMOITEMS_PROJECTILE_PARTICLES"), JsonObject.class);
                            Particle particle = Particle.valueOf(obj.get("Particle").getAsString());
                            // If the selected particle is colored, use the provided color
                            if (ProjectileParticlesData.isColorable(particle)) {
                                double red = Double.parseDouble(String.valueOf(obj.get("Red")));
                                double green = Double.parseDouble(String.valueOf(obj.get("Green")));
                                double blue = Double.parseDouble(String.valueOf(obj.get("Blue")));
                                ProjectileParticlesData.shootParticle(caster.getPlayer(), particle, loc.clone().add(loc.getDirection().multiply(1.5 * ti)), red, green, blue);
                                // If it's not colored, just shoot the particle
                            } else {
                                ProjectileParticlesData.shootParticle(caster.getPlayer(), particle, loc.clone().add(loc.getDirection().multiply(1.5 * ti)), 0, 0, 0);
                            }
                            // If no particle has been provided via projectile particle attribute, default to this particle
                        } else {
                            loc.getWorld().spawnParticle(Particle.NOTE, loc.clone().add(loc.getDirection().multiply(1.5 * ti)), 0);
                        }
                    }
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);

        for (Entity entity : MMOUtils.getNearbyChunkEntities(caster.getPlayer().getLocation()))
            if (entity.getLocation().distanceSquared(caster.getPlayer().getLocation()) < 40
                    && caster.getPlayer().getEyeLocation().getDirection().angle(entity.getLocation().toVector().subtract(caster.getPlayer().getLocation().toVector())) < Math.PI / 6
                    && UtilityMethods.canTarget(caster.getPlayer(), entity, InteractionType.OFFENSE_ACTION))
                caster.attack((LivingEntity) entity, damage, DamageType.WEAPON, DamageType.MAGIC, DamageType.PROJECTILE);
    }
}
