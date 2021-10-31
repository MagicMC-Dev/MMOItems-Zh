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
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Ice_Crystal extends VectorAbility {
    public Ice_Crystal() {
        super();

        addModifier("damage", 6);
        addModifier("duration", 3);
        addModifier("amplifier", 1);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, VectorAbilityMetadata ability) {
        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 1);
        new BukkitRunnable() {
            final Vector vec = ability.getTarget().multiply(.45);
            final Location loc = attack.getPlayer().getEyeLocation().clone().add(0, -.3, 0);
            int ti = 0;

            public void run() {
                if (ti++ > 25)
                    cancel();

                loc.getWorld().playSound(loc, Sound.BLOCK_GLASS_BREAK, 2, 1);
                List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
                for (int j = 0; j < 3; j++) {
                    loc.add(vec);
                    if (loc.getBlock().getType().isSolid())
                        cancel();

                    /*
                     * has a different particle effect since SNOW_DIG is not the
                     * same as in legacy minecraft, the particle effect is now a
                     * cross that rotates
                     */
                    for (double r = 0; r < .4; r += .1)
                        for (double a = 0; a < Math.PI * 2; a += Math.PI / 2) {
                            Vector vec = MMOUtils.rotateFunc(new Vector(r * Math.cos(a + (double) ti / 10), r * Math.sin(a + (double) ti / 10), 0),
                                    loc);
                            loc.add(vec);
                            loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, new Particle.DustOptions(Color.WHITE, .7f));
                            loc.add(vec.multiply(-1));
                        }

                    for (Entity entity : entities)
                        if (MMOUtils.canTarget(attack.getPlayer(), loc, entity)) {
                            loc.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, loc, 0);
                            loc.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc, 48, 0, 0, 0, .2);
                            loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 2, 1);
                            new AttackMetadata(new DamageMetadata(ability.getModifier("damage"), DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE), attack.getStats()).damage((LivingEntity) entity);
                            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW,
                                    (int) (ability.getModifier("duration") * 20), (int) ability.getModifier("amplifier")));
                            cancel();
                            return;
                        }
                }
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
