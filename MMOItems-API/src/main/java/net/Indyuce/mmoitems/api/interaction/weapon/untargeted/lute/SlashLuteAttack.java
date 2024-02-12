package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.lute;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.util.SoundReader;
import net.Indyuce.mmoitems.stat.data.ProjectileParticlesData;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class SlashLuteAttack implements LuteAttackHandler {

    @Override
    public void handle(PlayerMetadata caster, double damage, NBTItem nbt, double range, @NotNull Vector weight, @NotNull SoundReader sound, @NotNull ProjectileParticlesData projParticle) {
        new BukkitRunnable() {
            final Vector vec = caster.getPlayer().getEyeLocation().getDirection();
            final Location loc = caster.getPlayer().getLocation().add(0, 1.3, 0);
            double ti = 1;

            public void run() {
                if ((ti += .6) > 5) cancel();

                sound.play(loc, 2, (float) (.5 + ti / range));
                for (int k = -30; k < 30; k += 3)
                    if (RANDOM.nextBoolean()) {
                        loc.setDirection(vec);
                        loc.setYaw(loc.getYaw() + k);
                        loc.setPitch(caster.getPlayer().getEyeLocation().getPitch());

                        projParticle.shootParticle(loc.clone().add(loc.getDirection().multiply(1.5 * ti)));
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
