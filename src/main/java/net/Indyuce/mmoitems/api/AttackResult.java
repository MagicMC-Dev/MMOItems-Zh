package net.Indyuce.mmoitems.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.Validate;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability.CastingMode;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.comp.rpg.damage.DamageInfo;
import net.Indyuce.mmoitems.stat.type.ItemStat;

public class AttackResult {
	private final double initial;

	private double damage;
	private boolean successful;

	private final List<DamageType> damageTypes;

	private static final Random random = new Random();

	public AttackResult(boolean successful, DamageType... types) {
		this(successful, 0, types);
	}

	public AttackResult(double damage, DamageType... types) {
		this(true, damage, types);
	}

	public AttackResult(boolean successful, double damage, DamageType... types) {
		this(successful, damage, Arrays.asList(types));
	}

	public AttackResult(boolean successful, double damage, List<DamageType> types) {
		Validate.isTrue(types.size() > 0, "Attack must have at least one damage type!");

		this.successful = successful;
		this.initial = damage;
		this.damage = damage;

		this.damageTypes = types;
	}

	public AttackResult(AttackResult result) {
		initial = result.initial;
		damage = result.damage;
		successful = result.successful;
		damageTypes = new ArrayList<>(result.damageTypes);
	}

	public List<DamageType> getTypes() {
		return damageTypes;
	}

	public boolean hasType(DamageType type) {
		return damageTypes.contains(type);
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
		multiplyDamage(Math.max(1, 1 + coef));
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

	public void damage(TemporaryStats stats, LivingEntity target) {
		MMOItems.plugin.getDamage().damage(stats.getPlayer(), target, this);
	}

	public void applyEffectsAndDamage(TemporaryStats stats, NBTItem item, LivingEntity target) {
		MMOItems.plugin.getDamage().damage(stats.getPlayer(), target, applyEffects(stats, item, target));
	}

	/*
	 * this methods makes applying ALL effects including elemental damage easier
	 * for untargeted weapons like staffs.
	 */
	public AttackResult applyEffects(TemporaryStats stats, NBTItem item, LivingEntity target) {
		if (hasType(DamageType.WEAPON)) {
			applyElementalEffects(stats, item, target);
			applyOnHitEffects(stats, target);
		} else if (hasType(DamageType.SKILL))
			applySkillEffects(stats, target);
		return this;
	}

	public AttackResult applySkillEffects(TemporaryStats stats, LivingEntity target) {

		for (DamageType type : DamageType.values())
			if (hasType(type))
				addRelativeDamage(stats.getStat(type.getStat()) / 100);

		addRelativeDamage(stats.getStat(target instanceof Player ? ItemStat.PVP_DAMAGE : ItemStat.PVE_DAMAGE) / 100);
		if (MMOUtils.isUndead(target))
			addRelativeDamage(stats.getStat(ItemStat.UNDEAD_DAMAGE) / 100);

		return this;
	}

	public AttackResult applyElementalEffects(TemporaryStats stats, NBTItem item, LivingEntity target) {
		new ElementalAttack(item, this).applyElementalArmor(target).apply(stats);
		return this;
	}

	/*
	 * vanilla melee weapons have no NBTTitems so this method only provides for
	 * non-weapon specific effects like critical strikes and extra stat damage
	 */
	public AttackResult applyOnHitEffects(TemporaryStats stats, LivingEntity target) {

		// abilities
		stats.getPlayerData().castAbilities(stats, target, this, CastingMode.ON_HIT);

		// extra damage
		for (DamageType type : DamageType.values())
			if (hasType(type))
				addRelativeDamage(stats.getStat(type.getStat()) / 100);

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

	public DamageInfo toDamageInfo() {
		return new DamageInfo(damage, damageTypes);
	}

	public enum DamageType {

		/*
		 * skills or abilities dealing magic damage
		 */
		MAGICAL(ItemStat.MAGIC_DAMAGE),

		/*
		 * skills or abilities dealing physical damage
		 */
		PHYSICAL(ItemStat.PHYSICAL_DAMAGE),

		/*
		 * weapons dealing damage
		 */
		WEAPON(ItemStat.WEAPON_DAMAGE),

		/*
		 * skill damage
		 */
		SKILL(ItemStat.SKILL_DAMAGE),

		/*
		 * projectile based weapons or skills
		 */
		PROJECTILE(ItemStat.PROJECTILE_DAMAGE);

		private final ItemStat stat;

		private DamageType(ItemStat stat) {
			this.stat = stat;
		}

		public ItemStat getStat() {
			return stat;
		}
	}
}
