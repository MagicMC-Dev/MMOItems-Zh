package net.Indyuce.mmoitems.api;

import java.util.Random;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability.CastingMode;
import net.Indyuce.mmoitems.api.DamageInfo.DamageType;
import net.Indyuce.mmoitems.api.interaction.weapon.Weapon;
import net.Indyuce.mmoitems.api.interaction.weapon.untargeted.UntargetedWeapon;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class AttackResult {
	private final double initial;

	private double damage;
	private boolean successful, untargetedWeapon;

	private static final Random random = new Random();

	public AttackResult(boolean successful) {
		this(successful, 0);
	}

	public AttackResult(Weapon weapon, double damage) {
		this(true, damage);

		this.untargetedWeapon = weapon != null && weapon instanceof UntargetedWeapon;
	}

	public AttackResult(boolean successful, double damage) {
		this.successful = successful;
		this.damage = damage;
		this.initial = damage;
	}

	public AttackResult(AttackResult result) {
		initial = result.initial;
		damage = result.damage;
		successful = result.successful;
		untargetedWeapon = result.untargetedWeapon;
	}

	public boolean isSuccessful() {
		return successful;
	}

	public double getDamage() {
		return damage;
	}

	public boolean isDamageModified() {
		return initial != damage;
	}

	public void addDamage(double value) {
		damage += value;
	}

	public void addRelativeDamage(double coef) {
		multiplyDamage(1 + coef);
	}

	public AttackResult multiplyDamage(double coef) {
		damage *= coef;
		return this;
	}

	public AttackResult setSuccessful(boolean successful) {
		this.successful = successful;
		return this;
	}
	
	public AttackResult clone() {
		return new AttackResult(this);
	}

	public void applyEffectsAndDamage(TemporaryStats stats, NBTItem item, LivingEntity target, DamageType... types) {
		MMOItems.plugin.getDamage().damage(stats, target, applyEffects(stats, item, target).damage, types);
	}

	public AttackResult applyElementalEffects(TemporaryStats stats, NBTItem item, LivingEntity target) {
		new ElementalAttack(item, this).applyElementalArmor(target).apply(stats);
		return this;
	}

	/*
	 * this methods makes applying ALL effects including elemental damage easier
	 * for untargeted weapons like staffs.
	 */
	public AttackResult applyEffects(TemporaryStats stats, NBTItem item, LivingEntity target) {
		applyElementalEffects(stats, item, target);
		applyEffects(stats, target);
		return this;
	}

	/*
	 * vanilla melee weapons have no NBTTitems so this method only provides for
	 * non-weapon specific effects like critical strikes and extra stat damage
	 */
	public AttackResult applyEffects(TemporaryStats stats, LivingEntity target) {

		// abilities only if the weapon attacking is an untargeted weapon
		if (untargetedWeapon)
			stats.getPlayerData().castAbilities(stats, target, this, CastingMode.ON_HIT);

		// extra damage
		addRelativeDamage(stats.getStat(target instanceof Player ? ItemStat.PVP_DAMAGE : ItemStat.PVE_DAMAGE) / 100);
		addRelativeDamage(stats.getStat(ItemStat.WEAPON_DAMAGE) / 100);
		if (MMOItems.plugin.getDamage().isUndead(target))
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
