package net.Indyuce.mmoitems.ability;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.player.damage.AttackResult;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Heal extends Ability {
	public Heal() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("heal", 4);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
		stats.getPlayer().getWorld().spawnParticle(Particle.HEART, stats.getPlayer().getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
		stats.getPlayer().getWorld().spawnParticle(Particle.VILLAGER_HAPPY, stats.getPlayer().getLocation().add(0, .75, 0), 16, 1, 1, 1, 0);
		MMOUtils.heal(stats.getPlayer(), data.getModifier("heal"));
	}
}
