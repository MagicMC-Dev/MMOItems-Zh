package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Overload extends SimpleAbility {
    public Overload() {
        super();

        addModifier("damage", 6);
        addModifier("cooldown", 10);
        addModifier("radius", 6);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
        double damage = ability.getModifier("damage");
        double radius = ability.getModifier("radius");

        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 0);
        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_TWINKLE.toSound(), 2, 0);
        attack.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 254));

        for (Entity entity : attack.getPlayer().getNearbyEntities(radius, radius, radius))
            if (MMOUtils.canTarget(attack.getPlayer(), entity))
                new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC), attack.getStats()).damage((LivingEntity) entity);

        double step = 12 + (radius * 2.5);
        for (double j = 0; j < Math.PI * 2; j += Math.PI / step) {
            Location loc = attack.getPlayer().getLocation().clone().add(Math.cos(j) * radius, 1, Math.sin(j) * radius);
            attack.getPlayer().getWorld().spawnParticle(Particle.CLOUD, loc, 4, 0, 0, 0, .05);
            attack.getPlayer().getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 4, 0, 0, 0, .05);
        }
    }
}
