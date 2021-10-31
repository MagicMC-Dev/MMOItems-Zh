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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Fire_Meteor extends VectorAbility {
    public Fire_Meteor() {
        super();

        addModifier("damage", 6);
        addModifier("knockback", 1);
        addModifier("radius", 4);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, VectorAbilityMetadata ability) {
        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 3, 1);
        new BukkitRunnable() {
            final Location loc = attack.getPlayer().getLocation().clone().add(0, 10, 0);
            final Vector vec = ability.getTarget().multiply(1.3).setY(-1).normalize();
            double ti = 0;

            public void run() {
                ti++;
                if (ti > 40)
                    cancel();

                loc.add(vec);
                loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
                loc.getWorld().spawnParticle(Particle.FLAME, loc, 4, .2, .2, .2, 0);
                if (loc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid() || loc.getBlock().getType().isSolid()) {
                    loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 3, .6f);
                    loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 10, 2, 2, 2, 0);
                    loc.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, loc, 32, 0, 0, 0, .3);
                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 32, 0, 0, 0, .3);

                    double damage = ability.getModifier("damage");
                    double knockback = ability.getModifier("knockback");
                    double radius = ability.getModifier("radius");
                    for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
                        if (MMOUtils.canTarget(attack.getPlayer(), entity) && entity.getLocation().distanceSquared(loc) < radius * radius) {
                            new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE), attack.getStats()).damage((LivingEntity) entity);
                            entity.setVelocity(entity.getLocation().toVector().subtract(loc.toVector()).multiply(.1 * knockback).setY(.4 * knockback));
                        }
                    cancel();
                }
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
