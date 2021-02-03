package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.api.MMORayTraceResult;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import org.bukkit.Location;
import org.bukkit.Particle;

public class LightningSpirit implements StaffAttackHandler {

	@Override
	public void handle(CachedStats stats, NBTItem nbt, double attackDamage, double range) {
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 2, 2);

		Location loc = stats.getPlayer().getEyeLocation();
		MMORayTraceResult trace = MythicLib.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), range,
				entity -> MMOUtils.canDamage(stats.getPlayer(), entity));
		if (trace.hasHit())
			new ItemAttackResult(attackDamage, DamageType.WEAPON, DamageType.MAGIC).applyEffectsAndDamage(stats, nbt, trace.getHit());
		trace.draw(loc, stats.getPlayer().getEyeLocation().getDirection(), 2,
				loc1 -> loc1.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc1, 0));
	}
}
