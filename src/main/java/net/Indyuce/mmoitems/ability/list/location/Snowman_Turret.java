package net.Indyuce.mmoitems.ability.list.location;

import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.LocationAbility;
import net.Indyuce.mmoitems.ability.metadata.LocationAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.api.util.TemporaryListener;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Snowman_Turret extends LocationAbility {
    public Snowman_Turret() {
        super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
                CastingMode.SHIFT_RIGHT_CLICK);

        addModifier("duration", 6);
        addModifier("cooldown", 35);
        addModifier("damage", 2);
        addModifier("radius", 20);
        addModifier("mana", 0);
        addModifier("stamina", 0);
	}

    @Override
    public void whenCast(ItemAttackMetadata attack, LocationAbilityMetadata ability) {
        Location loc = ability.getTarget();
        double duration = Math.min(ability.getModifier("duration") * 20, 300);
        double radiusSquared = Math.pow(ability.getModifier("radius"), 2);

        loc.getWorld().playSound(loc, VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 2, 1);
        final Snowman snowman = (Snowman) loc.getWorld().spawnEntity(loc.add(0, 1, 0), EntityType.SNOWMAN);
        snowman.setInvulnerable(true);
        snowman.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 254, true));
        new BukkitRunnable() {
            int ti = 0;
            double j = 0;
			final TurretHandler turret = new TurretHandler(ability.getModifier("damage"));

			public void run() {
                if (ti++ > duration || attack.getDamager().isDead() || snowman == null || snowman.isDead()) {
                    turret.close(3 * 20);
                    snowman.remove();
                    cancel();
                }

                j += Math.PI / 24 % (2 * Math.PI);
                for (double k = 0; k < 3; k++)
                    snowman.getWorld().spawnParticle(Particle.SPELL_INSTANT,
                            snowman.getLocation().add(Math.cos(j + k / 3 * 2 * Math.PI) * 1.3, 1, Math.sin(j + k / 3 * 2 * Math.PI) * 1.3), 0);
                snowman.getWorld().spawnParticle(Particle.SPELL_INSTANT, snowman.getLocation().add(0, 1, 0), 1, 0, 0, 0, .2);

				if (ti % 2 == 0)
                    for (Entity entity : MMOUtils.getNearbyChunkEntities(snowman.getLocation()))
                        if (!entity.equals(snowman) && MMOUtils.canDamage(attack.getDamager(), entity)
                                && entity.getLocation().distanceSquared(snowman.getLocation()) < radiusSquared) {
                            snowman.getWorld().playSound(snowman.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1, 1.3f);
                            Snowball snowball = snowman.launchProjectile(Snowball.class);
                            snowball.setVelocity(entity.getLocation().add(0, entity.getHeight() / 2, 0).toVector()
                                    .subtract(snowman.getLocation().add(0, 1, 0).toVector()).normalize().multiply(1.3));
                            turret.entities.add(snowball.getUniqueId());
                            break;
                        }
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}

	public static class TurretHandler extends TemporaryListener {
		private final List<UUID> entities = new ArrayList<>();
		private final double damage;

		public TurretHandler(double damage) {
			super(EntityDamageByEntityEvent.getHandlerList());

			this.damage = damage;
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void a(EntityDamageByEntityEvent event) {
			if (entities.contains(event.getDamager().getUniqueId()))
				event.setDamage(damage);
		}
	}
}
