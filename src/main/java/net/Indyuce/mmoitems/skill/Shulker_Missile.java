package net.Indyuce.mmoitems.skill;

import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.skill.SkillMetadata;
import io.lumine.mythic.lib.skill.handler.SkillHandler;
import io.lumine.mythic.lib.skill.result.def.VectorSkillResult;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
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

import javax.annotation.Nullable;

public class Shulker_Missile extends SkillHandler<VectorSkillResult> implements Listener {
    public Shulker_Missile() {
        super();

        registerModifiers("damage", "effect-duration", "duration");
    }

    @Override
    public VectorSkillResult getResult(SkillMetadata meta) {
        return new VectorSkillResult(meta);
    }

    @Override
    public void whenCast(VectorSkillResult result, SkillMetadata skillMeta) {
        double duration = skillMeta.getModifier("duration");

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

                ItemAttackMetadata attackMeta = new ItemAttackMetadata(new DamageMetadata(skillMeta.getModifier("damage"), DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE), skillMeta.getCaster());
                MMOItems.plugin.getEntities().registerCustomEntity(shulkerBullet, new ShulkerMissileEntityData(attackMeta, skillMeta.getModifier("effect-duration")));

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
            if (!MMOUtils.canTarget(data.attackMeta.getPlayer(), null, entity, data.isWeaponAttack() ? InteractionType.OFFENSE_ACTION : InteractionType.OFFENSE_SKILL)) {
                event.setCancelled(true);
                return;
            }

            // Void spirit
            if (data.isWeaponAttack())
                data.attackMeta.applyEffects(data.weapon, entity);

            event.setDamage(data.attackMeta.getDamage().getDamage());

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
        private final ItemAttackMetadata attackMeta;
        private final double duration;

        @Nullable
        private final NBTItem weapon;

        /**
         * Used for the Shulker missile ability
         *
         * @param attackMeta Attack meta
         * @param duration   Duration of levitation effect in seconds
         */
        public ShulkerMissileEntityData(ItemAttackMetadata attackMeta, double duration) {
            this(attackMeta, duration, null);
        }

        /**
         * Used for the void staff attack spirit (no levitation effect)
         *
         * @param attackMeta Attack meta
         * @param weapon     Item used for the attack
         */
        public ShulkerMissileEntityData(ItemAttackMetadata attackMeta, NBTItem weapon) {
            this(attackMeta, 0, weapon);
        }

        private ShulkerMissileEntityData(ItemAttackMetadata attackMeta, double duration, NBTItem weapon) {
            this.attackMeta = attackMeta;
            this.duration = duration;
            this.weapon = weapon;
        }

        public boolean isWeaponAttack() {
            return attackMeta.getDamage().hasType(DamageType.WEAPON);
        }
    }
}
