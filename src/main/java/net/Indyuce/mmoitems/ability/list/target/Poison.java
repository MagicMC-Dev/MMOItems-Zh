package net.Indyuce.mmoitems.ability.list.target;

import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Poison extends TargetAbility {
    public Poison() {
        super();

        addModifier("duration", 4);
        addModifier("cooldown", 10);
        addModifier("amplifier", 1);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();

        target.getWorld().spawnParticle(Particle.SLIME, target.getLocation().add(0, 1, 0), 32, 1, 1, 1, 0);
        target.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, target.getLocation().add(0, 1, 0), 24, 1, 1, 1, 0);
        target.getWorld().playSound(target.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1.5f, 2);
        target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (ability.getModifier("duration") * 20), (int) ability.getModifier("amplifier")));
    }
}
