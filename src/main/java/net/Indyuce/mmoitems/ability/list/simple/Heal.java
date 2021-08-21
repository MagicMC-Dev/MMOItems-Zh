package net.Indyuce.mmoitems.ability.list.simple;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class Heal extends SimpleAbility {
    public Heal() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("heal", 4);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, SimpleAbilityMetadata ability) {
        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
        attack.getDamager().getWorld().spawnParticle(Particle.HEART, attack.getDamager().getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        attack.getDamager().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, attack.getDamager().getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        MMOUtils.heal(attack.getDamager(), ability.getModifier("heal"));
    }
}
