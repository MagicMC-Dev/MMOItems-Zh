package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Circular_Slash extends SimpleAbility {
    public Circular_Slash() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("damage", 6);
        addModifier("radius", 3);
        addModifier("knockback", 1);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, SimpleAbilityMetadata ability) {
        double damage = ability.getModifier("damage");
        double radius = ability.getModifier("radius");
        double knockback = ability.getModifier("knockback");

        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, .5f);
        attack.getDamager().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 254));
        for (Entity entity : attack.getDamager().getNearbyEntities(radius, radius, radius)) {
            if (MMOUtils.canTarget(attack.getDamager(), entity)) {
                new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.PHYSICAL), attack.getStats()).damage((LivingEntity) entity);
                Vector v1 = entity.getLocation().toVector();
                Vector v2 = attack.getDamager().getLocation().toVector();
                double y = .5;
                Vector v3 = v1.subtract(v2).multiply(.5 * knockback).setY(knockback == 0 ? 0 : y);
                entity.setVelocity(v3);
            }
        }
        double step = 12 + (radius * 2.5);
        for (double j = 0; j < Math.PI * 2; j += Math.PI / step) {
            Location loc = attack.getDamager().getLocation().clone();
            loc.add(Math.cos(j) * radius, .75, Math.sin(j) * radius);
            loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 0);
        }
        attack.getDamager().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, attack.getDamager().getLocation().add(0, 1, 0), 0);
    }
}
