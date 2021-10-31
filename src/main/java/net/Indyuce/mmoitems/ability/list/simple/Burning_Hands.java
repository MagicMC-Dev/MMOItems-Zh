package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Burning_Hands extends SimpleAbility {
    public Burning_Hands() {
        super();

        addModifier("duration", 3);
        addModifier("damage", 2);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
        double duration = ability.getModifier("duration") * 10;
        double damage = ability.getModifier("damage") / 2;

        new BukkitRunnable() {
            int j = 0;

            public void run() {
                if (j++ > duration)
                    cancel();

                Location loc = attack.getPlayer().getLocation().add(0, 1.2, 0);
                loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 1, 1);

                for (double m = -45; m < 45; m += 5) {
                    double a = (m + attack.getPlayer().getEyeLocation().getYaw() + 90) * Math.PI / 180;
                    Vector vec = new Vector(Math.cos(a), (random.nextDouble() - .5) * .2, Math.sin(a));
                    Location source = loc.clone().add(vec.clone().setY(0));
                    source.getWorld().spawnParticle(Particle.FLAME, source, 0, vec.getX(), vec.getY(), vec.getZ(), .5);
                    if (j % 2 == 0)
                        source.getWorld().spawnParticle(Particle.SMOKE_NORMAL, source, 0, vec.getX(), vec.getY(), vec.getZ(), .5);
                }

                if (j % 5 == 0)
                    for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
                        if (entity.getLocation().distanceSquared(loc) < 60
                                && attack.getPlayer().getEyeLocation().getDirection()
                                .angle(entity.getLocation().toVector().subtract(attack.getPlayer().getLocation().toVector())) < Math.PI / 6
                                && MMOUtils.canTarget(attack.getPlayer(), entity))
                            new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC), attack.getStats()).damage((LivingEntity) entity);

            }
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}
}
