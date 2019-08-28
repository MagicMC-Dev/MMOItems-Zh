package net.Indyuce.mmoitems.ability;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Grand_Heal extends Ability {
	public Grand_Heal() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("heal", 5);
		addModifier("radius", 5);
		addModifier("cooldown", 15);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		double heal = data.getModifier("heal");
		double radius = data.getModifier("radius");

		MMOUtils.heal(stats.getPlayer(), heal);
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
		stats.getPlayer().getWorld().spawnParticle(Particle.HEART, stats.getPlayer().getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
		stats.getPlayer().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, stats.getPlayer().getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
		for (Entity entity : stats.getPlayer().getNearbyEntities(radius, radius, radius))
			if (entity instanceof Player)
				MMOUtils.heal((Player) entity, heal);
	}
}
