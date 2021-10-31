package net.Indyuce.mmoitems.ability.list.target;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.ability.TargetAbility;
import net.Indyuce.mmoitems.ability.metadata.TargetAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class Sparkle extends TargetAbility {
    public Sparkle() {
        super();

        addModifier("cooldown", 10);
        addModifier("damage", 4);
        addModifier("limit", 5);
        addModifier("radius", 6);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, TargetAbilityMetadata ability) {
        LivingEntity target = ability.getTarget();
        double damage = ability.getModifier("damage");
        double radius = ability.getModifier("radius");
        double limit = ability.getModifier("limit");

        new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC), attack.getStats()).damage(target);
        target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().add(0, 1, 0), 0);
        target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_TWINKLE.toSound(), 2, 2);

        int count = 0;
        for (Entity entity : target.getNearbyEntities(radius, radius, radius))
            if (count < limit && entity instanceof LivingEntity && entity != attack.getPlayer() && !(entity instanceof ArmorStand)) {
                count++;
                new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC), attack.getStats()).damage((LivingEntity) entity);
                entity.getWorld().playSound(entity.getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_TWINKLE.toSound(), 2, 2);
                Location loc_t = target.getLocation().add(0, .75, 0);
                Location loc_ent = entity.getLocation().add(0, .75, 0);
                for (double j1 = 0; j1 < 1; j1 += .04) {
                    Vector d = loc_ent.toVector().subtract(loc_t.toVector());
                    target.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc_t.clone().add(d.multiply(j1)), 3, .1, .1, .1, .008);
                }
            }
    }
}
