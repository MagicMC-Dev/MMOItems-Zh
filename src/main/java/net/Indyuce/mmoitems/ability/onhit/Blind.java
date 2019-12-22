package net.Indyuce.mmoitems.ability.onhit;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.version.VersionSound;

public class Blind extends Ability {
	public Blind() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 5);
		addModifier("cooldown", 9);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		target = target == null ? MMOLib.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), 50, entity -> MMOUtils.canDamage(stats.getPlayer(), entity)).getHit() : target;
		if (target == null) {
			result.setSuccessful(false);
			return;
		}
		
		target.getWorld().playSound(target.getLocation(), VersionSound.ENTITY_ENDERMAN_HURT.toSound(), 1, 2);
		for (double i = 0; i < Math.PI * 2; i += Math.PI / 24)
			for (double j = 0; j < 2; j++) {
				Location loc = target.getLocation();
				Vector vec = MMOUtils.rotateFunc(new Vector(Math.cos(i), 1 + Math.cos(i + (Math.PI * j)) * .5, Math.sin(i)), stats.getPlayer().getLocation());
				MMOLib.plugin.getVersion().getWrapper().spawnParticle(Particle.REDSTONE, loc.add(vec), Color.BLACK);
			}
		target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (int) (data.getModifier("duration") * 20), 0));
	}
}
