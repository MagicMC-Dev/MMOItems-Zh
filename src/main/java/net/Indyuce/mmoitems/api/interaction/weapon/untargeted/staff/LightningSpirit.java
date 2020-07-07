package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import org.bukkit.Location;
import org.bukkit.Particle;

import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.mmogroup.mmolib.MMOLib;
import net.mmogroup.mmolib.api.DamageType;
import net.mmogroup.mmolib.api.MMORayTraceResult;
import net.mmogroup.mmolib.api.item.NBTItem;
import net.mmogroup.mmolib.version.VersionSound;

public class LightningSpirit implements StaffAttackHandler {

	@Override
	public void handle(CachedStats stats, NBTItem nbt, double attackDamage, double range) {
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 2, 2);

		Location loc = stats.getPlayer().getEyeLocation();
		MMORayTraceResult trace = MMOLib.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), range,
				entity -> MMOUtils.canDamage(stats.getPlayer(), entity));
		if (trace.hasHit())
			new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.MAGIC).applyEffectsAndDamage(stats, nbt, trace.getHit());
		trace.draw(loc, stats.getPlayer().getEyeLocation().getDirection(), 2,
				loc1 -> loc1.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc1, 0));
	}
}
