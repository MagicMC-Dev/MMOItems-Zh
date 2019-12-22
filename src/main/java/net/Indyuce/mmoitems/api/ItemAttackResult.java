package net.Indyuce.mmoitems.api;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability.CastingMode;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.item.NBTItem;

public class ItemAttackResult extends AttackResult {
	private static final Random random = new Random();

	public ItemAttackResult(boolean successful, DamageType... types) {
		this(successful, 0, types);
	}

	public ItemAttackResult(double damage, DamageType... types) {
		this(true, damage, types);
	}

	public ItemAttackResult(boolean successful, double damage, DamageType... types) {
		this(successful, damage, Arrays.asList(types));
	}

	public ItemAttackResult(boolean successful, double damage, List<DamageType> types) {
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

	public void damage(TemporaryStats stats, LivingEntity target) {
		MMOLib.plugin.getDamage().damage(stats.getPlayer(), target, this);
	}

	public void applyEffectsAndDamage(TemporaryStats stats, NBTItem item, LivingEntity target) {
		MMOLib.plugin.getDamage().damage(stats.getPlayer(), target, applyEffects(stats, item, target));
	}

	/*
	 * this methods makes applying ALL effects including elemental damage easier
	 * for untargeted weapons like staffs.
	 */
	public ItemAttackResult applyEffects(TemporaryStats stats, NBTItem item, LivingEntity target) {
		if (hasType(DamageType.WEAPON)) {
			applyElementalEffects(stats, item, target);
			applyOnHitEffects(stats, target);
		} else if (hasType(DamageType.SKILL))
			applySkillEffects(stats, target);
		return this;
	}

	public ItemAttackResult applySkillEffects(TemporaryStats stats, LivingEntity target) {

		for (DamageType type : DamageType.values())
			if (hasType(type))
				addRelativeDamage(stats.getStat((ItemStat) type.getMMOItemsStat()) / 100);

		addRelativeDamage(stats.getStat(target instanceof Player ? ItemStat.PVP_DAMAGE : ItemStat.PVE_DAMAGE) / 100);
		if (MMOUtils.isUndead(target))
			addRelativeDamage(stats.getStat(ItemStat.UNDEAD_DAMAGE) / 100);

		return this;
	}

	public ItemAttackResult applyElementalEffects(TemporaryStats stats, NBTItem item, LivingEntity target) {
		new ElementalAttack(item, this).applyElementalArmor(target).apply(stats);
		return this;
	}

	/*
	 * vanilla melee weapons have no NBTTitems so this method only provides for
	 * non-weapon specific effects like critical strikes and extra stat damage
	 */
	public ItemAttackResult applyOnHitEffects(TemporaryStats stats, LivingEntity target) {

		// abilities
		stats.getPlayerData().castAbilities(stats, target, this, CastingMode.ON_HIT);

		// extra damage
		for (DamageType type : DamageType.values())
			if (hasType(type))
				addRelativeDamage(stats.getStat((ItemStat) type.getMMOItemsStat()) / 100);

		addRelativeDamage(stats.getStat(target instanceof Player ? ItemStat.PVP_DAMAGE : ItemStat.PVE_DAMAGE) / 100);
		if (MMOUtils.isUndead(target))
			addRelativeDamage(stats.getStat(ItemStat.UNDEAD_DAMAGE) / 100);

		// critical strikes
		if (random.nextDouble() <= stats.getStat(ItemStat.CRITICAL_STRIKE_CHANCE) / 100) {
			multiplyDamage(MMOItems.plugin.getConfig().getDouble("crit-coefficient") + stats.getStat(ItemStat.CRITICAL_STRIKE_POWER) / 100);
			target.getWorld().playSound(target.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1, 1);
			target.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, target.getLocation().add(0, 1, 0), 16, 0, 0, 0, .1);
		}

		return this;
	}

}
