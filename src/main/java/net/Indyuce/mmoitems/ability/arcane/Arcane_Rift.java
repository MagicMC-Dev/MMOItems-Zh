package net.Indyuce.mmoitems.ability.arcane;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.SimpleAbilityResult;
import net.Indyuce.mmoitems.api.ability.VectorAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.version.VersionSound;

public class Arcane_Rift extends Ability {
	public Arcane_Rift() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 5);
		addModifier("amplifier", 2);
		addModifier("cooldown", 10);
		addModifier("speed", 1);
		addModifier("duration", 1.5);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		if (!stats.getPlayer().isOnGround())
			return new SimpleAbilityResult(ability, false);

		return new VectorAbilityResult(ability, stats.getPlayer(), target);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		double damage = ability.getModifier("damage");
		double slowDuration = ability.getModifier("duration");
		double slowAmplifier = ability.getModifier("amplifier");

		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_DEATH.toSound(), 2, .5f);
		new BukkitRunnable() {
			Vector vec = ((VectorAbilityResult) ability).getTarget().setY(0).normalize().multiply(.5 * ability.getModifier("speed"));
			Location loc = stats.getPlayer().getLocation();
			int ti = 0, duration = (int) (20 * Math.min(ability.getModifier("duration"), 10.));
			List<Integer> hit = new ArrayList<>();

			public void run() {
				if (ti++ > duration)
					cancel();

				loc.add(vec);
				loc.getWorld().spawnParticle(Particle.SPELL_WITCH, loc, 5, .5, 0, .5, 0);

				for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
					if (MMOUtils.canDamage(stats.getPlayer(), entity) && loc.distanceSquared(entity.getLocation()) < 2 && !hit.contains(entity.getEntityId())) {
						hit.add(entity.getEntityId());
						new AttackResult(damage, DamageType.SKILL, DamageType.MAGICAL).damage(stats.getPlayer(), (LivingEntity) entity);
						((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (slowDuration * 20), (int) slowAmplifier));
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
