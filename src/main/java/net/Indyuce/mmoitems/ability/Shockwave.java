package net.Indyuce.mmoitems.ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.SimpleAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Shockwave extends Ability {
	public Shockwave() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
				CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 7.5);
		addModifier("knock-up", 1);
		addModifier("length", 5);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new SimpleAbilityResult(ability);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		double knockUp = ability.getModifier("knock-up");
		double length = ability.getModifier("length");

		new BukkitRunnable() {
			final Vector vec = stats.getPlayer().getEyeLocation().getDirection().setY(0);
			final Location loc = stats.getPlayer().getLocation();
			int ti = 0;
			final List<Integer> hit = new ArrayList<>();

			public void run() {
				ti++;
				if (ti >= Math.min(20, length))
					cancel();

				loc.add(vec);

				loc.getWorld().playSound(loc, Sound.BLOCK_GRAVEL_BREAK, 1, 2);
				loc.getWorld().spawnParticle(Particle.BLOCK_CRACK, loc, 12, .5, 0, .5, 0, Material.DIRT.createBlockData());

				for (Entity ent : MMOUtils.getNearbyChunkEntities(loc))
					if (ent.getLocation().distance(loc) < 1.1 && ent instanceof LivingEntity && !ent.equals(stats.getPlayer())
							&& !hit.contains(ent.getEntityId())) {
						hit.add(ent.getEntityId());
						ent.playEffect(EntityEffect.HURT);
						ent.setVelocity(ent.getVelocity().setY(.4 * knockUp));
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
