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

public class Sky_Smash extends SimpleAbility {
    public Sky_Smash() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("cooldown", 10);
        addModifier("damage", 3);
        addModifier("knock-up", 1);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, SimpleAbilityMetadata ability) {
        double damage = ability.getModifier("damage");
        double knockUp = ability.getModifier("knock-up");

        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, .5f);
        attack.getDamager().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 254));
        Location loc = attack.getDamager().getEyeLocation().add(attack.getDamager().getEyeLocation().getDirection().multiply(3));
        loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
        loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 16, 0, 0, 0, .1);

        for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
            if (MMOUtils.canDamage(attack.getDamager(), entity) && entity.getLocation().distanceSquared(loc) < 10) {
                new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.PHYSICAL), attack.getStats()).damage((LivingEntity) entity);
                Location loc1 = attack.getDamager().getEyeLocation().clone();
                loc1.setPitch(-70);
                entity.setVelocity(loc1.getDirection().multiply(1.2 * knockUp));
            }
    }
}
