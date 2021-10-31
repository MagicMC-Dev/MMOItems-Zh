package net.Indyuce.mmoitems.ability.list.vector;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.VectorAbility;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Thrust extends VectorAbility {
    public Thrust() {
        super();

        addModifier("cooldown", 10);
        addModifier("damage", 6);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, VectorAbilityMetadata ability) {
        double damage = ability.getModifier("damage");

        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 0);
        attack.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 3));

        Location loc = attack.getPlayer().getEyeLocation().clone();
        Vector vec = ability.getTarget().multiply(.5);
        for (double j = 0; j < 7; j += .5) {
            loc.add(vec);
            for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
                if (MMOUtils.canTarget(attack.getPlayer(), loc, entity))
                    new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.PHYSICAL), attack.getStats()).damage((LivingEntity) entity);
            loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 0);
        }
    }
}
