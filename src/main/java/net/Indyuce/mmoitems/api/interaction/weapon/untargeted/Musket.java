package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.interaction.util.UntargetedDurabilityItem;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.MMORayTraceResult;
import net.mmogroup.mmolib.api.item.NBTItem;

public class Musket extends UntargetedWeapon {
	public Musket(Player player, NBTItem item, Type type) {
		super(player, item, type, WeaponType.RIGHT_CLICK);
	}

	@Override
	public void untargetedAttack(EquipmentSlot slot) {
		CachedStats stats = getPlayerData().getStats().newTemporary();

		if (!hasEnoughResources(1 / getValue(stats.getStat(ItemStat.ATTACK_SPEED), MMOItems.plugin.getConfig().getDouble("default.attack-speed")), CooldownType.ATTACK, false))
			return;
		
		UntargetedDurabilityItem durItem = new UntargetedDurabilityItem(getPlayer(), getNBTItem(), slot);
		if (durItem.isValid())
			durItem.decreaseDurability(1).update();

		double attackDamage = stats.getStat(ItemStat.ATTACK_DAMAGE);
		double range = getValue(getNBTItem().getStat(ItemStat.RANGE), MMOItems.plugin.getConfig().getDouble("default.range"));
		double recoil = getValue(getNBTItem().getStat(ItemStat.RECOIL), MMOItems.plugin.getConfig().getDouble("default.recoil"));

		// knockback
		double knockback = getNBTItem().getStat(ItemStat.KNOCKBACK);
		if (knockback > 0)
			getPlayer().setVelocity(getPlayer().getVelocity().add(getPlayer().getEyeLocation().getDirection().setY(0).normalize().multiply(-1 * knockback).setY(-.2)));

		double a = Math.toRadians(getPlayer().getEyeLocation().getYaw() + 160);
		Location loc = getPlayer().getEyeLocation().add(new Vector(Math.cos(a), 0, Math.sin(a)).multiply(.5));

		loc.setPitch((float) (loc.getPitch() + (random.nextDouble() - .5) * 2 * recoil));
		loc.setYaw((float) (loc.getYaw() + (random.nextDouble() - .5) * 2 * recoil));
		Vector vec = loc.getDirection();

		MMORayTraceResult trace = MMOLib.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), vec, range, entity -> MMOUtils.canDamage(stats.getPlayer(), entity));
		if (trace.hasHit())
			new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.PHYSICAL).applyEffectsAndDamage(stats, getNBTItem(), trace.getHit());
		trace.draw(loc, vec, 2, Color.BLACK);
		getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, 2);
	}
}
