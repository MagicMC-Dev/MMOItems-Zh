package net.Indyuce.mmoitems.ability.list.simple;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Grand_Heal extends SimpleAbility {
    public Grand_Heal() {
        super();

        addModifier("heal", 5);
        addModifier("radius", 5);
        addModifier("cooldown", 15);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
        double heal = ability.getModifier("heal");
        double radius = ability.getModifier("radius");

        MMOUtils.heal(attack.getPlayer(), heal);
        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
        attack.getPlayer().getWorld().spawnParticle(Particle.HEART, attack.getPlayer().getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        attack.getPlayer().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, attack.getPlayer().getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
        for (Entity entity : attack.getPlayer().getNearbyEntities(radius, radius, radius))
            if (entity instanceof Player)
                MMOUtils.heal((Player) entity, heal);
    }
}
