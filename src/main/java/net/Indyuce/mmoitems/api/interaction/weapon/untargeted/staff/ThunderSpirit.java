package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ThunderSpirit implements StaffAttackHandler {

    @Override
    public void handle(ItemAttackMetadata attackMeta, NBTItem nbt, EquipmentSlot slot, double range) {
        attackMeta.getPlayer().getWorld().playSound(attackMeta.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
        new BukkitRunnable() {
            final Location target = getGround(attackMeta.getPlayer().getTargetBlock(null, (int) range * 2).getLocation()).add(0, 1.2, 0);
            final double a = RANDOM.nextDouble() * Math.PI * 2;
            final Location loc = target.clone().add(Math.cos(a) * 4, 10, Math.sin(a) * 4);
            final Vector vec = target.toVector().subtract(loc.toVector()).multiply(.015);
            double ti = 0;

            public void run() {
                loc.getWorld().playSound(loc, VersionSound.BLOCK_NOTE_BLOCK_HAT.toSound(), 2, 2);
                for (int j = 0; j < 4; j++) {
                    ti += .015;
                    loc.add(vec);
                    loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 0, .03, 0, .03, 0);
                    if (ti >= 1) {
                        loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 24, 0, 0, 0, .12);
                        loc.getWorld().playSound(loc, VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 2, 2);
                        for (Entity target : MMOUtils.getNearbyChunkEntities(loc))
                            if (MMOUtils.canTarget(attackMeta.getPlayer(), target, InteractionType.OFFENSE_ACTION) && target.getLocation().distanceSquared(loc) <= 9)
                                attackMeta.clone().applyEffectsAndDamage(nbt, (LivingEntity) target);
                        cancel();
                    }
                }
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
    }
}
