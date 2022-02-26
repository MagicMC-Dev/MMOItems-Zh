package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.target.InteractionType;
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

import java.util.List;

public class NetherSpirit implements StaffAttackHandler {

    @Override
    public void handle(ItemAttackMetadata attackMeta, NBTItem nbt, EquipmentSlot slot, double range) {
        new BukkitRunnable() {
            final Vector vec = attackMeta.getPlayer().getEyeLocation().getDirection().multiply(.3);
            final Location loc = attackMeta.getPlayer().getEyeLocation();
            int ti = 0;

            public void run() {
                if (ti++ % 2 == 0)
                    loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 2, 2);
                List<Entity> targets = MMOUtils.getNearbyChunkEntities(loc);
                for (int j = 0; j < 3; j++) {
                    loc.add(vec);
                    if (loc.getBlock().getType().isSolid()) {
                        cancel();
                        break;
                    }

                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 2, .07, .07, .07, 0);
                    loc.getWorld().spawnParticle(Particle.SMOKE_NORMAL, loc, 0);
                    for (Entity target : targets)
                        if (MMOUtils.canTarget(attackMeta.getPlayer(), loc, target, InteractionType.OFFENSE_ACTION)) {
                            attackMeta.applyEffectsAndDamage(nbt, (LivingEntity) target);
                            loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
                            cancel();
                            return;
                        }
                }
                if (ti >= range)
                    cancel();
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
    }
}
