package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.api.MMORayTraceResult;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.interaction.util.UntargetedDurabilityItem;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.listener.ItemUse;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

public class Musket extends UntargetedWeapon {
	public Musket(Player player, NBTItem item) {
		super(player, item, WeaponType.RIGHT_CLICK);
	}

	@Override
	public void untargetedAttack(EquipmentSlot slot) {
		if (!ItemUse.eitherHandSuccess(getPlayer(), getNBTItem(), slot))
			return;
		CachedStats stats = getPlayerData().getStats().newTemporary();

		if (!applyWeaponCosts(1 / getValue(stats.getStat(ItemStats.ATTACK_SPEED), MMOItems.plugin.getConfig().getDouble("default.attack-speed")),
				CooldownType.ATTACK))
			return;

		UntargetedDurabilityItem durItem = new UntargetedDurabilityItem(getPlayer(), getNBTItem(), slot);
		if (durItem.isBroken())
			return;

		if (durItem.isValid())
			durItem.decreaseDurability(1).update();

		double attackDamage = stats.getStat(ItemStats.ATTACK_DAMAGE);
		double range = getValue(getNBTItem().getStat(ItemStats.RANGE.getId()), MMOItems.plugin.getConfig().getDouble("default.range"));
		double recoil = getValue(getNBTItem().getStat(ItemStats.RECOIL.getId()), MMOItems.plugin.getConfig().getDouble("default.recoil"));

		// knockback
		double knockback = getNBTItem().getStat(ItemStats.KNOCKBACK.getId());
		if (knockback > 0)
			getPlayer().setVelocity(getPlayer().getVelocity()
					.add(getPlayer().getEyeLocation().getDirection().setY(0).normalize().multiply(-1 * knockback).setY(-.2)));

		double a = Math.toRadians(getPlayer().getEyeLocation().getYaw() + 160);
		Location loc = getPlayer().getEyeLocation().add(new Vector(Math.cos(a), 0, Math.sin(a)).multiply(.5));

		loc.setPitch((float) (loc.getPitch() + (RANDOM.nextDouble() - .5) * 2 * recoil));
		loc.setYaw((float) (loc.getYaw() + (RANDOM.nextDouble() - .5) * 2 * recoil));
		Vector vec = loc.getDirection();

		MMORayTraceResult trace = MythicLib.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), vec, range,
				entity -> MMOUtils.canDamage(stats.getPlayer(), entity));
		if (trace.hasHit())
			new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.PHYSICAL).applyEffectsAndDamage(stats,
					getNBTItem(), trace.getHit());
		trace.draw(loc, vec, 2, Color.BLACK);
		getPlayer().getWorld().playSound(getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 2, 2);
	}
}
