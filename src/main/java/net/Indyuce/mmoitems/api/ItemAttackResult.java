package net.Indyuce.mmoitems.api;

import java.util.Set;

import org.bukkit.entity.LivingEntity;

import net.Indyuce.mmoitems.api.Ability.CastingMode;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.item.NBTItem;

public class ItemAttackResult extends AttackResult {
	public ItemAttackResult(boolean successful, DamageType... types) {
		super(successful, 0, types);
	}

	public ItemAttackResult(double damage, DamageType... types) {
		super(true, damage, types);
	}

	public ItemAttackResult(boolean successful, double damage, DamageType... types) {
		super(successful, damage, types);
	}

	public ItemAttackResult(boolean successful, double damage, Set<DamageType> types) {
		super(successful, damage, types);
	}

	public ItemAttackResult(ItemAttackResult result) {
		super(result);
	}

	@Override
	public ItemAttackResult clone() {
		return new ItemAttackResult(this);
	}

	@Override
	public ItemAttackResult setSuccessful(boolean successful) {
		return (ItemAttackResult) super.setSuccessful(successful);
	}

	@Override
	public ItemAttackResult multiplyDamage(double coef) {
		return (ItemAttackResult) super.multiplyDamage(coef);
	}

	public void applyEffectsAndDamage(CachedStats stats, NBTItem item, LivingEntity target) {
		MMOLib.plugin.getDamage().damage(stats.getPlayer(), target, applyEffects(stats, item, target));
	}

	/*
	 * this methods makes applying ALL effects including elemental damage easier
	 * for untargeted weapons like staffs.
	 */
	public ItemAttackResult applyEffects(CachedStats stats, NBTItem item, LivingEntity target) {
		if (hasType(DamageType.WEAPON)) {
			applyElementalEffects(stats, item, target);
			applyOnHitEffects(stats, target);
		}
		return this;
	}

	public ItemAttackResult applyElementalEffects(CachedStats stats, NBTItem item, LivingEntity target) {
		new ElementalAttack(item, this).applyElementalArmor(target).apply(stats);
		return this;
	}

	/*
	 * vanilla melee weapons have no NBT tags so this method only provides for
	 * non-weapon specific effects like critical strikes and extra stat damage
	 */
	public ItemAttackResult applyOnHitEffects(CachedStats stats, LivingEntity target) {
		stats.getPlayerData().castAbilities(stats, target, this, CastingMode.ON_HIT);
		return this;
	}
}
