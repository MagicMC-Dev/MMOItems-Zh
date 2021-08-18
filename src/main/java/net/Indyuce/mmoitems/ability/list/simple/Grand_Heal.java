package net.Indyuce.mmoitems.ability.list.simple;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Grand_Heal extends SimpleAbility {
    public Grand_Heal() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("heal", 5);
        addModifier("radius", 5);
        addModifier("cooldown", 15);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, SimpleAbilityMetadata ability) {
        double heal = ability.getModifier("heal");
        double radius = ability.getModifier("radius");

        MMOUtils.heal(attack.getDamager(), heal);
        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
        attack.getDamager().getWorld().spawnParticle(Particle.HEART, attack.getDamager().getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        attack.getDamager().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, attack.getDamager().getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        for (Entity entity : attack.getDamager().getNearbyEntities(radius, radius, radius))
            if (entity instanceof Player)
                MMOUtils.heal((Player) entity, heal);
    }
}
