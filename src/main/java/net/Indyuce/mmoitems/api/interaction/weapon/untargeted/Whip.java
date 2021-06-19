package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.interaction.util.UntargetedDurabilityItem;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.api.MMORayTraceResult;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionSound;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;

public class Whip extends UntargetedWeapon {
	public Whip(Player player, NBTItem item) {
		super(player, item, WeaponType.LEFT_CLICK);
	}

	@Override
	public void untargetedAttack(EquipmentSlot slot) {

		CachedStats stats = getPlayerData().getStats().newTemporary(io.lumine.mythic.lib.api.player.EquipmentSlot.fromBukkit(slot));
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

		double a = Math.toRadians(getPlayer().getEyeLocation().getYaw() + 160);
		Location loc = getPlayer().getEyeLocation().add(new Vector(Math.cos(a), 0, Math.sin(a)).multiply(.5));

		MMORayTraceResult trace = MythicLib.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), range,
				entity -> MMOUtils.canDamage(stats.getPlayer(), entity));
		if (trace.hasHit())
			new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.PHYSICAL).applyEffectsAndDamage(stats,
					getNBTItem(), trace.getHit());
		trace.draw(loc, getPlayer().getEyeLocation().getDirection(), 2,
				(tick) -> tick.getWorld().spawnParticle(Particle.CRIT, tick, 0, .1, .1, .1, 0));
		getPlayer().getWorld().playSound(getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 2);
	}
}
