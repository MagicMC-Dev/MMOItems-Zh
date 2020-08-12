package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.TargetAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.version.VersionSound;

public class Blind extends Ability {
	public Blind() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
				CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 5);
		addModifier("cooldown", 9);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new TargetAbilityResult(ability, stats.getPlayer(), target);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		LivingEntity target = ((TargetAbilityResult) ability).getTarget();

		target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_ENDERMAN_HURT.toSound(), 1, 2);
		for (double i = 0; i < Math.PI * 2; i += Math.PI / 24)
			for (double j = 0; j < 2; j++) {
				Location loc = target.getLocation();
				Vector vec = MMOUtils.rotateFunc(new Vector(Math.cos(i), 1 + Math.cos(i + (Math.PI * j)) * .5, Math.sin(i)),
						stats.getPlayer().getLocation());
				loc.getWorld().spawnParticle(Particle.REDSTONE, loc.add(vec), 1, new Particle.DustOptions(Color.BLACK, 1));
			}
		target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (ability.getModifier("duration") * 20), 0));
	}
}
