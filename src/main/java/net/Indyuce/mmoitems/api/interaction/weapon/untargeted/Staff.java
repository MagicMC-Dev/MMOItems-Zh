package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.api.MMORayTraceResult;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.interaction.util.UntargetedDurabilityItem;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.StaffSpiritStat.StaffSpirit;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

public class Staff extends UntargetedWeapon {
	public Staff(Player player, NBTItem item) {
		super(player, item, WeaponType.LEFT_CLICK);
	}

	@Override
	public void untargetedAttack(EquipmentSlot slot) {

		CachedStats stats = getPlayerData().getStats().newTemporary();
		if (!applyWeaponCosts(1 / getValue(stats.getStat(ItemStats.ATTACK_SPEED), MMOItems.plugin.getConfig().getDouble("default.attack-speed")),
				CooldownType.ATTACK))
			return;

		UntargetedDurabilityItem durItem = new UntargetedDurabilityItem(getPlayer(), getNBTItem(), slot);
		if (durItem.isBroken())
			return;

		if (durItem.isValid())
			durItem.decreaseDurability(1).update();

		double attackDamage = getValue(stats.getStat(ItemStats.ATTACK_DAMAGE), 1);
		double range = getValue(getNBTItem().getStat(ItemStats.RANGE.getId()), MMOItems.plugin.getConfig().getDouble("default.range"));

		StaffSpirit spirit = StaffSpirit.get(getNBTItem());
		if (spirit != null) {
			spirit.getAttack().handle(stats, getNBTItem(), attackDamage, range);
			return;
		}

		double a = Math.toRadians(getPlayer().getEyeLocation().getYaw() + 160);
		Location loc = getPlayer().getEyeLocation().add(new Vector(Math.cos(a), 0, Math.sin(a)).multiply(.5));

		MMORayTraceResult trace = MythicLib.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), range,
				entity -> MMOUtils.canDamage(stats.getPlayer(), entity));
		if (trace.hasHit())
			new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.MAGIC).applyEffectsAndDamage(stats, getNBTItem(),
					trace.getHit());
		trace.draw(loc, getPlayer().getEyeLocation().getDirection(), 2,
				(tick) -> tick.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, tick, 0, .1, .1, .1, 0));
		getPlayer().getWorld().playSound(getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_TWINKLE.toSound(), 2, 2);
	}

	public void specialAttack(LivingEntity target) {
		if (!MMOItems.plugin.getConfig().getBoolean("item-ability.staff.enabled"))
			return;

		if (!applyWeaponCosts(MMOItems.plugin.getConfig().getDouble("item-ability.staff.cooldown"), CooldownType.SPECIAL_ATTACK))
			return;

		double power = MMOItems.plugin.getConfig().getDouble("item-ability.staff.power");

		try {
			Vector vec = target.getLocation().toVector().subtract(getPlayer().getLocation().toVector()).setY(0).normalize().multiply(1.75 * power).setY(.65 * power);
			target.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, target.getLocation().add(0, 1, 0), 0);
			target.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, target.getLocation().add(0, 1, 0), 16, 0, 0, 0, .1);
			target.setVelocity(vec);
			target.playEffect(EntityEffect.HURT);
			target.getWorld().playSound(target.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 2);
		} catch (IllegalArgumentException ignored) {}
	}
}