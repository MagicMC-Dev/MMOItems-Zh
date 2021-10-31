package net.Indyuce.mmoitems.ability.list.vector;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.VectorAbility;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Holy_Missile extends VectorAbility {
    public Holy_Missile() {
        super();

        addModifier("damage", 6);
        addModifier("cooldown", 10);
        addModifier("duration", 4);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, VectorAbilityMetadata ability) {
        double duration = ability.getModifier("duration") * 10;
        double damage = ability.getModifier("damage");

        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 1);
        new BukkitRunnable() {
            final Vector vec = ability.getTarget().multiply(.45);
            final Location loc = attack.getPlayer().getLocation().clone().add(0, 1.3, 0);
            double ti = 0;

            public void run() {
                if (ti++ > duration)
                    cancel();

                loc.getWorld().playSound(loc, VersionSound.BLOCK_NOTE_BLOCK_HAT.toSound(), 2, 1);
                List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
                for (int j = 0; j < 2; j++) {
                    loc.add(vec);
                    if (loc.getBlock().getType().isSolid())
                        cancel();

                    for (double i = -Math.PI; i < Math.PI; i += Math.PI / 2) {
                        Vector v = new Vector(Math.cos(i + ti / 4), Math.sin(i + ti / 4), 0);
                        v = MMOUtils.rotateFunc(v, loc);
                        loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 0, v.getX(), v.getY(), v.getZ(), .08);
                    }

                    for (Entity entity : entities)
                        if (MMOUtils.canTarget(attack.getPlayer(), loc, entity)) {
                            loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 1);
                            loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 32, 0, 0, 0, .2);
                            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
                            new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE), attack.getStats()).damage((LivingEntity) entity);
                            cancel();
                            return;
                        }
                }
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}

