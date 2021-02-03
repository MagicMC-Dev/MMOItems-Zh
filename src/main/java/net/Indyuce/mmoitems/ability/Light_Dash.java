package net.Indyuce.mmoitems.ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Particle;
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
import io.lumine.mythic.lib.api.AttackResult;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.version.VersionSound;

public class Light_Dash extends Ability {
	public Light_Dash() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 3);
		addModifier("cooldown", 10);
		addModifier("length", 1);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new SimpleAbilityResult(ability);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		double damage = ability.getModifier("damage");
		double length = ability.getModifier("length");

		new BukkitRunnable() {
			int j = 0;
			final Vector vec = stats.getPlayer().getEyeLocation().getDirection();
			final List<Integer> hit = new ArrayList<>();

			public void run() {
				if (j++ > 10 * Math.min(10, length))
					cancel();

				stats.getPlayer().setVelocity(vec);
				stats.getPlayer().getWorld().spawnParticle(Particle.SMOKE_LARGE, stats.getPlayer().getLocation().add(0, 1, 0), 0);
				stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 1, 2);
				for (Entity entity : stats.getPlayer().getNearbyEntities(1, 1, 1))
					if (!hit.contains(entity.getEntityId()) && MMOUtils.canDamage(stats.getPlayer(), entity)) {
						hit.add(entity.getEntityId());
						new AttackResult(damage, DamageType.SKILL, DamageType.PHYSICAL).damage(stats.getPlayer(), (LivingEntity) entity);
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}
}
