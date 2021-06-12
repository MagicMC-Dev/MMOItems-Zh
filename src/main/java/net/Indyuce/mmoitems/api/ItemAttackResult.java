package net.Indyuce.mmoitems.api;

import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.AttackResult;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.ability.Ability.CastingMode;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;

public class ItemAttackResult extends AttackResult {
	public ItemAttackResult(boolean successful, DamageType... types) {
		super(successful, 0, types);
	}

	public ItemAttackResult(double damage, DamageType... types) {
		super(true, damage, types);
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

	/**
	 * Applies on-hit effects and deals damage to the target
	 * 
	 * @param stats
	 *            Player doing the attack
	 * @param item
	 *            The item being used
	 * @param target
	 *            The entity target
	 */
	public void applyEffectsAndDamage(CachedStats stats, NBTItem item, LivingEntity target) {
		MythicLib.plugin.getDamage().damage(stats.getPlayer(), target, applyEffects(stats, item, target));
	}

	/**
	 * Applies all necessary on-hit effects for any type of damage. Makes things
	 * much easier for untargeted weapons like staffs
	 * 
	 * @param stats
	 *            Player doing the attack
	 * @param item
	 *            The item being used
	 * @param target
	 *            The entity target
	 * @return The unedited attack result
	 */
	public ItemAttackResult applyEffects(CachedStats stats, NBTItem item, LivingEntity target) {
		if (hasType(DamageType.WEAPON)) {
			applyElementalEffects(stats, item, target);
			applyOnHitEffects(stats, target);
		}
		return this;
	}

	/**
	 * Applies weapon specific on-hit effects like elemental damage.
	 * 
	 * @param stats
	 *            Player doing the attack
	 * @param item
	 *            The item being used
	 * @param target
	 *            The entity target
	 * @return The unedited attack result
	 */
	@SuppressWarnings("UnusedReturnValue")
	public ItemAttackResult applyElementalEffects(CachedStats stats, NBTItem item, LivingEntity target) {
		new ElementalAttack(item, this, target).apply(stats);
		return this;
	}

	/**
	 * This method is called when a player uses ANY weapon, vanilla or custom.
	 * It does not take into input any weapon as it just applies non weapon
	 * specific on-hit effects
	 * 
	 * @param stats
	 *            Player doing the attack
	 * @param target
	 *            The entity target
	 * @return The unedited attack result
	 */
	public ItemAttackResult applyOnHitEffects(CachedStats stats, LivingEntity target) {
		stats.getData().castAbilities(stats, target, this, CastingMode.ON_HIT);
		return this;
	}
}
