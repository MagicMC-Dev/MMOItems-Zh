package net.Indyuce.mmoitems.ability.list.simple;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Shockwave extends SimpleAbility {
    public Shockwave() {
        super();

        addModifier("cooldown", 7.5);
        addModifier("knock-up", 1);
        addModifier("length", 5);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
        double knockUp = ability.getModifier("knock-up");
        double length = ability.getModifier("length");

        new BukkitRunnable() {
            final Vector vec = attack.getPlayer().getEyeLocation().getDirection().setY(0);
            final Location loc = attack.getPlayer().getLocation();
            final List<Integer> hit = new ArrayList<>();
            int ti = 0;

            public void run() {
                ti++;
                if (ti >= Math.min(20, length))
                    cancel();

                loc.add(vec);

                loc.getWorld().playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 1, 2);
				loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 12, .5, 0, .5, 0, Material.DIRT.createBlockData());

                for (Entity ent : MMOUtils.getNearbyChunkEntities(loc))
                    if (ent.getLocation().distance(loc) < 1.1 && ent instanceof LivingEntity && !ent.equals(attack.getPlayer())
                            && !hit.contains(ent.getEntityId())) {
                        hit.add(ent.getEntityId());
                        ent.playEffect(EntityEffect.HURT);
                        ent.setVelocity(ent.getVelocity().setY(.4 * knockUp));
                    }
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
