package net.Indyuce.mmoitems.ability;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.SimpleAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.version.VersionSound;

public class Blink extends Ability {
	public Blink() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("range", 8);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new SimpleAbilityResult(ability);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		stats.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, stats.getPlayer().getLocation().add(0, 1, 0), 0);
		stats.getPlayer().getWorld().spawnParticle(Particle.SPELL_INSTANT, stats.getPlayer().getLocation().add(0, 1, 0), 32, 0, 0, 0, .1);
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 1, 1);
		Location loc = stats.getPlayer().getTargetBlock((Set<Material>) null, (int) ability.getModifier("range")).getLocation().add(0, 1, 0);
		loc.setYaw(stats.getPlayer().getLocation().getYaw());
		loc.setPitch(stats.getPlayer().getLocation().getPitch());
		stats.getPlayer().teleport(loc);
		stats.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, stats.getPlayer().getLocation().add(0, 1, 0), 0);
		stats.getPlayer().getWorld().spawnParticle(Particle.SPELL_INSTANT, stats.getPlayer().getLocation().add(0, 1, 0), 32, 0, 0, 0, .1);
	}
}
