package net.Indyuce.mmoitems.ability;

import org.bukkit.entity.LivingEntity;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;

public class Smite extends Ability {
	public Smite() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 10);
		addModifier("damage", 8);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(CachedStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		target = target == null ? MMOLib.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), 50, entity -> MMOUtils.canDamage(stats.getPlayer(), entity)).getHit() : target;
		if (target == null) {
			result.setSuccessful(false);
			return;
		}

		new AttackResult(data.getModifier("damage"), DamageType.SKILL, DamageType.MAGICAL).damage(stats.getPlayer(), target);
		target.getWorld().strikeLightningEffect(target.getLocation());
	}
}
