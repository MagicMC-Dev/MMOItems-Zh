package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.DamageType;

public class Thrust extends Ability {
	public Thrust() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 10);
		addModifier("damage", 6);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(CachedStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		double damage = data.getModifier("damage");

		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 1, 0);
		stats.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 2, 3));

		Location loc = stats.getPlayer().getEyeLocation().clone();
		Vector vec = getTargetDirection(stats.getPlayer(), target).multiply(.5);
		for (double j = 0; j < 7; j += .5) {
			loc.add(vec);
			for (Entity entity : MMOUtils.getNearbyChunkEntities(loc))
				if (MMOUtils.canDamage(stats.getPlayer(), loc, entity))
					new ItemAttackResult(damage, DamageType.SKILL, DamageType.PHYSICAL).applyEffectsAndDamage(stats, null, (LivingEntity) entity);
			loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 0);
		}
	}
}
