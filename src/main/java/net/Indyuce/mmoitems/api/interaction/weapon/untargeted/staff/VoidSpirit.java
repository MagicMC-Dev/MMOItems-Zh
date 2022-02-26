package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.skill.Shulker_Missile;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class VoidSpirit implements StaffAttackHandler {

    @Override
    public void handle(ItemAttackMetadata attackMeta, NBTItem nbt, EquipmentSlot slot, double range) {
        Vector vec = attackMeta.getPlayer().getEyeLocation().getDirection();
        attackMeta.getPlayer().getWorld().playSound(attackMeta.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
        ShulkerBullet shulkerBullet = (ShulkerBullet) attackMeta.getPlayer().getWorld().spawnEntity(attackMeta.getPlayer().getLocation().add(0, 1, 0), EntityType.valueOf("SHULKER_BULLET"));
        shulkerBullet.setShooter(attackMeta.getPlayer());
        new BukkitRunnable() {
            double ti = 0;

            public void run() {
                ti += .1;
                if (shulkerBullet.isDead() || ti >= range / 4) {
                    shulkerBullet.remove();
                    cancel();
                }
                shulkerBullet.setVelocity(vec);
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
        MMOItems.plugin.getEntities().registerCustomEntity(shulkerBullet, new Shulker_Missile.ShulkerMissileEntityData(attackMeta, nbt));
    }
}
