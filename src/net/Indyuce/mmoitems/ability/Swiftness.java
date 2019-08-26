package net.Indyuce.mmoitems.ability;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.VersionSound;

public class Swiftness extends Ability {
	public Swiftness() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 15);
		addModifier("duration", 4);
		addModifier("amplifier", 1);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		double duration = data.getModifier("duration");
		int amplifier = (int) data.getModifier("amplifier");

		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ZOMBIE_PIGMAN_ANGRY.toSound(), 1, .3f);
		for (double y = 0; y <= 2; y += .2)
			for (double j = 0; j < Math.PI * 2; j += Math.PI / 16)
				if (random.nextDouble() <= .7)
					stats.getPlayer().getWorld().spawnParticle(Particle.SPELL_INSTANT, stats.getPlayer().getLocation().add(Math.cos(j), y, Math.sin(j)), 0);
		stats.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) (duration * 20), amplifier));
		stats.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (duration * 20), amplifier));
	}
}
