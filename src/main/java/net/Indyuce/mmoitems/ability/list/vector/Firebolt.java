package net.Indyuce.mmoitems.ability.list.vector;

import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.VectorAbility;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Firebolt extends VectorAbility {
    public Firebolt() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("damage", 6);
        addModifier("ignite", 3);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(ItemAttackMetadata attack, VectorAbilityMetadata ability) {
        attack.getDamager().getWorld().playSound(attack.getDamager().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 1);
        new BukkitRunnable() {
            final Vector vec = ability.getTarget().multiply(.8);
            final Location loc = attack.getDamager().getEyeLocation();
            int ti = 0;

            public void run() {
                ti++;
                if (ti > 20)
                    cancel();

                List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
                loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_AMBIENT, 2, 1);
                for (int j = 0; j < 2; j++) {
                    loc.add(vec);
                    if (loc.getBlock().getType().isSolid())
                        cancel();

                    loc.getWorld().spawnParticle(Particle.FLAME, loc, 5, .12, .12, .12, 0);
                    if (random.nextDouble() < .3)
                        loc.getWorld().spawnParticle(Particle.LAVA, loc, 0);
                    for (Entity target : entities)
                        if (MMOUtils.canTarget(attack.getDamager(), loc, target)) {
                            loc.getWorld().spawnParticle(Particle.FLAME, loc, 32, 0, 0, 0, .1);
                            loc.getWorld().spawnParticle(Particle.LAVA, loc, 8, 0, 0, 0, 0);
                            loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
                            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 3, 1);
                            new AttackMetadata(new DamageMetadata(ability.getModifier("damage"), DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE), attack.getStats()).damage((LivingEntity) target);
                            target.setFireTicks((int) ability.getModifier("ignite") * 20);
                            cancel();
                            return;
                        }
                }
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
