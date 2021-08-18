package net.Indyuce.mmoitems.ability.list.vector;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.VectorAbility;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Heavy_Charge extends VectorAbility {
    public Heavy_Charge() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("damage", 6);
        addModifier("knockback", 1);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, VectorAbilityMetadata ability) {
        double knockback = ability.getModifier("knockback");

        new BukkitRunnable() {
            final Vector vec = ability.getTarget().setY(-1);
            double ti = 0;

            public void run() {
                if (ti++ > 20)
                    cancel();

                if (ti < 9) {
                    attack.getDamager().setVelocity(vec);
                    attack.getDamager().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, attack.getDamager().getLocation().add(0, 1, 0), 3, .13, .13, .13, 0);
                }

                for (Entity target : attack.getDamager().getNearbyEntities(1, 1, 1))
                    if (MMOUtils.canDamage(attack.getDamager(), target)) {
                        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 1);
                        attack.getDamager().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().add(0, 1, 0), 0);
                        target.setVelocity(attack.getDamager().getVelocity().setY(0.3).multiply(1.7 * knockback));
                        attack.getDamager().setVelocity(attack.getDamager().getVelocity().setX(0).setY(0).setZ(0));
                        new AttackMetadata(new DamageMetadata(ability.getModifier("damage"), DamageType.SKILL, DamageType.PHYSICAL), attack.getStats()).damage((LivingEntity) target);
                        cancel();
                        break;
                    }
            }
        }.runTaskTimer(MMOItems.plugin, 0, 1);
    }
}
