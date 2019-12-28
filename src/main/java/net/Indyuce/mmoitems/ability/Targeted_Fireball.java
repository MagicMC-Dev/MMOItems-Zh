package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.TargetAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
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
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new TargetAbilityResult(ability, stats.getPlayer(), target);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		LivingEntity target = ((TargetAbilityResult) ability).getTarget();

		new BukkitRunnable() {
			int j = 0;
			Location loc = stats.getPlayer().getLocation().add(0, 1.3, 0);

			public void run() {
				j++;
				if (target.isDead() || !target.getWorld().equals(loc.getWorld()) || j > 200) {
					cancel();
					return;
				}

				Vector dir = target.getLocation().add(0, target.getHeight() / 2, 0).subtract(loc).toVector().normalize();
				loc.add(dir.multiply(.6));

				loc.setDirection(dir);
				for (double a = 0; a < Math.PI * 2; a += Math.PI / 6) {
					Vector rotated = MMOUtils.rotateFunc(new Vector(Math.cos(a), Math.sin(a), 0), loc);
					loc.getWorld().spawnParticle(Particle.FLAME, loc, 0, rotated.getX(), rotated.getY(), rotated.getZ(), .06);
				}

				loc.getWorld().playSound(loc, VersionSound.BLOCK_NOTE_BLOCK_HAT.toSound(), 1, 1);
				if (target.getLocation().distanceSquared(loc) < 1.3) {
					loc.getWorld().spawnParticle(Particle.LAVA, loc, 8);
					loc.getWorld().spawnParticle(Particle.FLAME, loc, 32, 0, 0, 0, .1);
					loc.getWorld().playSound(loc, Sound.ENTITY_BLAZE_HURT, 2, 1);
					target.setFireTicks((int) (target.getFireTicks() + ability.getModifier("ignite") * 20));
					new AttackResult(ability.getModifier("damage"), DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE).damage(stats.getPlayer(), target);
					cancel();
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}