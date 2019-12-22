package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Snowman;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.version.VersionSound;

public class Snowman_Turret extends Ability {
	public Snowman_Turret() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 6);
		addModifier("cooldown", 35);
		addModifier("damage", 2);
		addModifier("radius", 20);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		Location loc = getTargetLocation(stats.getPlayer(), target);
		if (loc == null) {
			result.setSuccessful(false);
			return;
		}

		double duration = Math.min(data.getModifier("duration") * 20, 300);
		double radiusSquared = Math.pow(data.getModifier("radius"), 2);
		double damage1 = data.getModifier("damage");

		loc.getWorld().playSound(loc, VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 2, 1);
		final Snowman snowman = (Snowman) loc.getWorld().spawnEntity(loc.add(0, 1, 0), EntityType.SNOWMAN);
		snowman.setInvulnerable(true);
		snowman.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100000, 254, true));
		new BukkitRunnable() {
			int ti = 0;
			double j = 0;

			public void run() {
				if (ti++ > duration || stats.getPlayer().isDead() || snowman == null || snowman.isDead()) {
					snowman.remove();
					cancel();
				}

				j += Math.PI / 24 % (2 * Math.PI);
				for (double k = 0; k < 3; k++)
					snowman.getWorld().spawnParticle(Particle.SPELL_INSTANT, snowman.getLocation().add(Math.cos(j + k / 3 * 2 * Math.PI) * 1.3, 1, Math.sin(j + k / 3 * 2 * Math.PI) * 1.3), 0);
				snowman.getWorld().spawnParticle(Particle.SPELL_INSTANT, snowman.getLocation().add(0, 1, 0), 1, 0, 0, 0, .2);

				if (ti % 2 == 0)
					for (Entity entity : snowman.getWorld().getEntities())
						if (!entity.equals(snowman) && MMOUtils.canDamage(stats.getPlayer(), entity) && entity.getLocation().distanceSquared(snowman.getLocation()) < radiusSquared) {
							snowman.getWorld().playSound(snowman.getLocation(), Sound.ENTITY_SNOWBALL_THROW, 1, 1.3f);
							Snowball snowball = snowman.launchProjectile(Snowball.class);
							snowball.setVelocity(entity.getLocation().add(0, entity.getHeight() / 2, 0).toVector().subtract(snowman.getLocation().add(0, 1, 0).toVector()).normalize().multiply(1.3));
							MMOItems.plugin.getEntities().registerCustomEntity(snowball, damage1);
							break;
						}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
