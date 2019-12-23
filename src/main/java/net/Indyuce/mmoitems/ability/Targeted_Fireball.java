package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.version.VersionSound;

public class Targeted_Fireball extends Ability {
	public Targeted_Fireball() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
		addModifier("ignite", 4);
		addModifier("damage", 4);
	}

	@Override
	public void whenCast(CachedStats stats, LivingEntity initialTarget, AbilityData data, ItemAttackResult result) {
		LivingEntity target = initialTarget == null ? MMOLib.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), 50, entity -> MMOUtils.canDamage(stats.getPlayer(), entity)).getHit() : initialTarget;
		if (target == null) {
			result.setSuccessful(false);
			return;
		}

		new BukkitRunnable() {
			int j = 0;
			Location loc = stats.getPlayer().getLocation().add(0, 1.3, 0);

			public void run() {
				j++;
				if (target.isDead() || !target.getWorld().equals(loc.getWorld()) || j > 200) {
					cancel();
					return;
				}

				loc.add(target.getLocation().add(0, target.getHeight() / 2, 0).subtract(loc).toVector().normalize().multiply(.6));

				loc.getWorld().playSound(loc, VersionSound.BLOCK_NOTE_BLOCK_HAT.toSound(), 1, 1);
				for (double a = 0; a < Math.PI * 2; a += Math.PI / 6)
					loc.getWorld().spawnParticle(Particle.FLAME, loc, 0, Math.cos(a), Math.sin(a), 0, .06);

				if (target.getLocation().distanceSquared(loc) < 1.3) {
					loc.getWorld().spawnParticle(Particle.LAVA, loc, 8);
					loc.getWorld().spawnParticle(Particle.FLAME, loc, 32, 0, 0, 0, .1);
					loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_HURT, 2, 1);
					target.setFireTicks((int) (target.getFireTicks() + data.getModifier("ignite") * 20));
					new AttackResult(data.getModifier("damage"), DamageType.SKILL, DamageType.MAGICAL, DamageType.PROJECTILE).damage(stats.getPlayer(), target);
					cancel();
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}