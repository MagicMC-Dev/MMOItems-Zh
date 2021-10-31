package net.Indyuce.mmoitems.ability.list.simple;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Particle;
import org.bukkit.Sound;

public class Heal extends SimpleAbility {
    public Heal() {
        super();

        addModifier("heal", 4);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
        attack.getPlayer().getWorld().spawnParticle(Particle.HEART, attack.getPlayer().getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        attack.getPlayer().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, attack.getPlayer().getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        MMOUtils.heal(attack.getPlayer(), ability.getModifier("heal"));
    }
}
