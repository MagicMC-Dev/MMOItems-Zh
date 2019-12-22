package net.Indyuce.mmoitems.ability;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.player.damage.AttackResult;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.VersionSound;

public class Blink extends Ability {
	public Blink() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("range", 8);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		stats.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, stats.getPlayer().getLocation().add(0, 1, 0), 0);
		stats.getPlayer().getWorld().spawnParticle(Particle.SPELL_INSTANT, stats.getPlayer().getLocation().add(0, 1, 0), 32, 0, 0, 0, .1);
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 1, 1);
		Location loc = stats.getPlayer().getTargetBlock((Set<Material>) null, (int) data.getModifier("range")).getLocation().add(0, 1, 0);
		loc.setYaw(stats.getPlayer().getLocation().getYaw());
		loc.setPitch(stats.getPlayer().getLocation().getPitch());
		stats.getPlayer().teleport(loc);
		stats.getPlayer().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, stats.getPlayer().getLocation().add(0, 1, 0), 0);
		stats.getPlayer().getWorld().spawnParticle(Particle.SPELL_INSTANT, stats.getPlayer().getLocation().add(0, 1, 0), 32, 0, 0, 0, .1);
	}
}
