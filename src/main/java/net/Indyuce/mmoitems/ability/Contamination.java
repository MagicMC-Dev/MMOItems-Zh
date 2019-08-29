package net.Indyuce.mmoitems.ability;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.AttackResult.DamageType;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.VersionSound;

public class Contamination extends Ability {
	public Contamination() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 2);
		addModifier("duration", 8);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		Location loc = getTargetLocation(stats.getPlayer(), target);
		if (loc == null) {
			result.setSuccessful(false);
			return;
		}

		double duration = Math.min(30, data.getModifier("duration")) * 20;

		loc.add(0, .1, 0);
		new BukkitRunnable() {
			double ti = 0;
			int j = 0;
			double dps = data.getModifier("damage") / 2;

			public void run() {
				j++;
				if (j >= duration)
					cancel();

				MMOItems.plugin.getVersion().getVersionWrapper().spawnParticle(Particle.REDSTONE, loc.clone().add(Math.cos(ti / 3) * 5, 0, Math.sin(ti / 3) * 5), Color.PURPLE);
				for (int j = 0; j < 3; j++) {
					ti += Math.PI / 32;
					double r = Math.sin(ti / 2) * 4;
					for (double k = 0; k < Math.PI * 2; k += Math.PI * 2 / 3)
						loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc.clone().add(r * Math.cos(k + ti / 4), 0, r * Math.sin(k + ti / 4)), 0);
				}

				if (j % 10 == 0) {
					loc.getWorld().playSound(loc, VersionSound.ENTITY_ENDERMAN_HURT.toSound(), 2, 1);
					for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
						if (MMOUtils.canDamage(stats.getPlayer(), entity) && entity.getLocation().distanceSquared(loc) <= 25)
							MMOItems.plugin.getDamage().damage(stats.getPlayer(), (LivingEntity) entity, new AttackResult(dps, DamageType.SKILL, DamageType.MAGICAL).applySkillEffects(stats, (LivingEntity) entity), false);
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
