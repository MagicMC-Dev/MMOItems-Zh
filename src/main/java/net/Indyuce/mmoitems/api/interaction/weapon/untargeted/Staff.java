package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.AttackResult.DamageType;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.util.MMORayTraceResult;
import net.Indyuce.mmoitems.stat.Staff_Spirit.StaffSpirit;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.version.VersionSound;

public class Staff extends UntargetedWeapon {
	public Staff(Player player, NBTItem item, Type type) {
		super(player, item, type, WeaponType.LEFT_CLICK);
	}

	@Override
	public void untargetedAttackEffects() {
		TemporaryStats stats = getPlayerData().getStats().newTemporary();

		if (!hasEnoughResources(1 / getValue(stats.getStat(ItemStat.ATTACK_SPEED), MMOItems.plugin.getConfig().getDouble("default.attack-speed")), CooldownType.ATTACK, false))
			return;

		double attackDamage = getValue(stats.getStat(ItemStat.ATTACK_DAMAGE), 1);
		double range = getValue(getNBTItem().getStat(ItemStat.RANGE), MMOItems.plugin.getConfig().getDouble("default.range"));

		StaffSpirit spirit = StaffSpirit.get(getNBTItem());
		if (spirit != null) {
			spirit.getAttack().handle(stats, getNBTItem(), attackDamage, range);
			return;
		}

		double a = Math.toRadians(getPlayer().getEyeLocation().getYaw() + 160);
		Location loc = getPlayer().getEyeLocation().add(new Vector(Math.cos(a), 0, Math.sin(a)).multiply(.5));

		MMORayTraceResult trace = MMOItems.plugin.getVersion().getVersionWrapper().rayTrace(stats.getPlayer(), range);
		if (trace.hasHit())
			new AttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.MAGICAL).applyEffectsAndDamage(stats, getNBTItem(), trace.getHit());
		trace.draw(loc, getPlayer().getEyeLocation().getDirection(), 2, (tick) -> tick.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, tick, 0, .1, .1, .1, 0));
		getPlayer().getWorld().playSound(getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_TWINKLE.toSound(), 2, 2);
	}

	public void specialAttack(LivingEntity target) {
		if (!MMOItems.plugin.getConfig().getBoolean("item-ability.staff.enabled"))
			return;

		if (!hasEnoughResources(MMOItems.plugin.getConfig().getDouble("item-ability.staff.cooldown"), CooldownType.SPECIAL_ATTACK, false))
			return;

		double power = MMOItems.plugin.getConfig().getDouble("item-ability.staff.power");
		Vector vec = target.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).setY(0).normalize().multiply(1.75 * power).setY(.65 * power);
		target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().add(0, 1, 0), 0);
		target.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, target.getLocation().add(0, 1, 0), 16, 0, 0, 0, .1);
		target.setVelocity(vec);
		target.playEffect(EntityEffect.HURT);
		target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 2);
	}
}