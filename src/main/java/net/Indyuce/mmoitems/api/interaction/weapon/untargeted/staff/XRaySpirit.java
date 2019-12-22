package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.player.damage.AttackResult;
import net.Indyuce.mmoitems.api.player.damage.AttackResult.DamageType;
import net.Indyuce.mmoitems.api.util.MMORayTraceResult;

public class XRaySpirit implements StaffAttackHandler {

	@Override
	public void handle(TemporaryStats stats, NBTItem nbt, double attackDamage, double range) {
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 2, 2);

		double a = Math.toRadians(stats.getPlayer().getEyeLocation().getYaw() + 160);
		Location loc = stats.getPlayer().getEyeLocation().add(new Vector(Math.cos(a), 0, Math.sin(a)).multiply(.5));

		MMORayTraceResult trace = MMOItems.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), range);
		if (trace.hasHit())
			new AttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.MAGICAL).applyEffectsAndDamage(stats, nbt, trace.getHit());
		trace.draw(loc, stats.getPlayer().getEyeLocation().getDirection(), 2, Color.BLACK);
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.40f, 2);
	}
}
