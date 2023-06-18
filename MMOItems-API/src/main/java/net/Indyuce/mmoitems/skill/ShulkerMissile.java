package net.Indyuce.mmoitems.skill;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.interaction.InteractionType;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.player.PlayerMetadata;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.interaction.projectile.EntityData;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ShulkerMissile extends SkillHandler<VectorSkillResult> implements Listener {
    public ShulkerMissile() {
        super("SHULKER_MISSILE");

        registerModifiers("damage", "effect-duration", "duration");
    }

    @NotNull
    @Override
    public VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getParameter("duration");

        Player caster = skillMeta.getCaster().getPlayer();

        new BukkitRunnable() {
            double n = 0;

            public void run() {
                if (n++ > 3) {
                    cancel();
                    return;
                }

                Vector vec = result.getTarget();
                caster.getWorld().playSound(caster.getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
                ShulkerBullet shulkerBullet = (ShulkerBullet) caster.getWorld().spawnEntity(caster.getLocation().add(0, 1, 0),
                        EntityType.SHULKER_BULLET);
                shulkerBullet.setShooter(caster);

                MMOItems.plugin.getEntities().registerCustomEntity(shulkerBullet, new ShulkerMissileEntityData(skillMeta.getCaster(), new DamageMetadata(skillMeta.getParameter("damage"), DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE), skillMeta.getParameter("effect-duration"), null));

                new BukkitRunnable() {
                    double ti = 0;

                    public void run() {
                        if (shulkerBullet.isDead() || ti++ >= duration * 20) {
                            shulkerBullet.remove();
                            cancel();
                        } else
                            shulkerBullet.setVelocity(vec);
                    }
                }.runTaskTimer(MMOItems.plugin, 0, 1);
            }
        }.runTaskTimer(MMOItems.plugin, 0, 3);
    }

    @EventHandler
    public void a(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof ShulkerBullet && event.getEntity() instanceof LivingEntity) {
            ShulkerBullet damager = (ShulkerBullet) event.getDamager();
            LivingEntity entity = (LivingEntity) event.getEntity();
            if (!MMOItems.plugin.getEntities().isCustomEntity(damager))
                return;

            ShulkerMissileEntityData data = (ShulkerMissileEntityData) MMOItems.plugin.getEntities().getEntityData(damager);
            if (!UtilityMethods.canTarget(data.caster.getPlayer(), null, entity, data.isWeaponAttack() ? InteractionType.OFFENSE_ACTION : InteractionType.OFFENSE_SKILL)) {
                event.setCancelled(true);
                return;
            }

            event.setDamage(data.damage.getDamage());

            new BukkitRunnable() {
                final Location loc = entity.getLocation();
                double y = 0;

                public void run() {

                    // Potion effect should apply right after the damage with a 1 tick delay.
                    if (y == 0) {
                        entity.removePotionEffect(PotionEffectType.LEVITATION);
                        entity.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, (int) (data.duration * 20), 0));
                    }

                    for (int j1 = 0; j1 < 3; j1++) {
                        y += .04;
                        for (int j = 0; j < 2; j++) {
                            double xz = y * Math.PI * 1.3 + (j * Math.PI);
                            loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone().add(Math.cos(xz), y, Math.sin(xz)), 1,
                                    new Particle.DustOptions(Color.MAROON, 1));
                        }
                    }
                    if (y >= 2)
                        cancel();
                }
            }.runTaskTimer(MMOItems.plugin, 0, 1);
        }
    }

    public static class ShulkerMissileEntityData implements EntityData {
        private final PlayerMetadata caster;
        private final DamageMetadata damage;
        private final double duration;

        @Nullable
        private final NBTItem weapon;

        public ShulkerMissileEntityData(PlayerMetadata caster, DamageMetadata damage, double duration, NBTItem weapon) {
            this.caster = caster;
            this.damage = damage;
            this.duration = duration;
            this.weapon = weapon;
        }

        public boolean isWeaponAttack() {
            return damage.hasType(DamageType.WEAPON);
        }
    }
}
