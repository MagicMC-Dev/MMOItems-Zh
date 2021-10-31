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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

public class Cursed_Beam extends VectorAbility {
    public Cursed_Beam() {
        super();

        addModifier("damage", 8);
        addModifier("cooldown", 10);
        addModifier("duration", 5);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, VectorAbilityMetadata ability) {
        double duration = ability.getModifier("duration");

        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
        new BukkitRunnable() {
            final Vector dir = ability.getTarget().multiply(.3);
            final Location loc = attack.getPlayer().getEyeLocation().clone();
            final double r = 0.4;
            int ti = 0;

            public void run() {
                ti++;
                if (ti > 50)
                    cancel();

                List<Entity> entities = MMOUtils.getNearbyChunkEntities(loc);
                for (double j = 0; j < 4; j++) {
                    loc.add(dir);
                    for (double i = 0; i < Math.PI * 2; i += Math.PI / 6) {
                        Vector vec = MMOUtils.rotateFunc(new Vector(r * Math.cos(i), r * Math.sin(i), 0), loc);
                        loc.add(vec);
                        loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 0);
                        loc.add(vec.multiply(-1));
                    }

                    for (Entity target : entities)
                        if (MMOUtils.canTarget(attack.getPlayer(), loc, target)) {
                            effect(target);
                            double damage = ability.getModifier("damage");
                            loc.getWorld().playSound(loc, VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 2, .7f);

                            for (Entity entity : entities)
                                if (MMOUtils.canTarget(attack.getPlayer(), entity) && loc.distanceSquared(entity.getLocation().add(0, 1, 0)) < 9) {
                                    new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE), attack.getStats()).damage((LivingEntity) entity);
                                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (duration * 20), 0));
                                }
                            cancel();
                            return;
                        }
                }
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}

	private void effect(Entity ent) {
		new BukkitRunnable() {
			final Location loc2 = ent.getLocation();
			double y = 0;

			public void run() {
				for (int i = 0; i < 3; i++) {
					y += .05;
					for (int j = 0; j < 2; j++) {
						double xz = y * Math.PI * .8 + (j * Math.PI);
						loc2.getWorld().spawnParticle(Particle.SPELL_WITCH, loc2.clone().add(Math.cos(xz) * 2.5, y, Math.sin(xz) * 2.5), 0);
					}
				}
				if (y >= 3)
					cancel();
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
