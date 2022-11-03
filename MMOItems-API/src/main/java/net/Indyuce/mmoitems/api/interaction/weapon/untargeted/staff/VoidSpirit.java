package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.skill.ShulkerMissile;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class VoidSpirit implements StaffAttackHandler {

    @Override
    public void handle(PlayerMetadata caster, double damage, NBTItem nbt, EquipmentSlot slot, double range) {
        Vector vec = caster.getPlayer().getEyeLocation().getDirection();
        caster.getPlayer().getWorld().playSound(caster.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
        ShulkerBullet shulkerBullet = (ShulkerBullet) caster.getPlayer().getWorld().spawnEntity(caster.getPlayer().getLocation().add(0, 1, 0), EntityType.valueOf("SHULKER_BULLET"));
        shulkerBullet.setShooter(caster.getPlayer());
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
        MMOItems.plugin.getEntities().registerCustomEntity(shulkerBullet, new ShulkerMissile.ShulkerMissileEntityData(caster, new DamageMetadata(damage, DamageType.WEAPON, DamageType.MAGIC, DamageType.PROJECTILE), 0, nbt));
    }
}
