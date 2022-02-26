package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.comp.target.InteractionType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class ManaSpirit implements StaffAttackHandler {

    @Override
    public void handle(ItemAttackMetadata attackMeta, NBTItem nbt, EquipmentSlot slot, double range) {
        new BukkitRunnable() {
            final Vector vec = attackMeta.getPlayer().getEyeLocation().getDirection().multiply(.4);
            final Location loc = attackMeta.getPlayer().getEyeLocation();
            int ti = 0;
            final double r = .2;

            public void run() {
                if (ti++ > range)
                    cancel();

                if (ti % 2 == 0)
                    loc.getWorld().playSound(loc, Sound.BLOCK_SNOW_BREAK, 2, 2);
                List<Entity> targets = MMOUtils.getNearbyChunkEntities(loc);
                for (int j = 0; j < 3; j++) {
                    loc.add(vec);
                    if (loc.getBlock().getType().isSolid()) {
                        cancel();
                        break;
                    }

                    for (double item = 0; item < Math.PI * 2; item += Math.PI / 3.5) {
                        Vector vec = MMOUtils.rotateFunc(new Vector(r * Math.cos(item), r * Math.sin(item), 0), loc);
                        if (RANDOM.nextDouble() <= .6)
                            loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone().add(vec), 1, new Particle.DustOptions(Color.AQUA, 1));
                    }
                    for (Entity target : targets)
                        if (MMOUtils.canTarget(attackMeta.getPlayer(), loc, target, InteractionType.OFFENSE_ACTION)) {
                            attackMeta.applyEffectsAndDamage(nbt, (LivingEntity) target);
                            loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
                            cancel();
                            return;
                        }
                }
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
    }
}
