package net.Indyuce.mmoitems.api.interaction.weapon.untargeted.staff;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.MMORayTraceResult;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.comp.target.InteractionType;
import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import org.bukkit.Location;
import org.bukkit.Particle;

public class LightningSpirit implements StaffAttackHandler {

	@Override
	public void handle(ItemAttackMetadata attackMeta, NBTItem nbt, double attackDamage, double range) {
		attackMeta.getDamager().getWorld().playSound(attackMeta.getDamager().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 2, 2);

		Location loc = attackMeta.getDamager().getEyeLocation();
		MMORayTraceResult trace = MythicLib.plugin.getVersion().getWrapper().rayTrace(attackMeta.getDamager(), range,
				entity -> MMOUtils.canTarget(attackMeta.getDamager(), entity, InteractionType.OFFENSE_ACTION));
		if (trace.hasHit())
			attackMeta.applyEffectsAndDamage(nbt, trace.getHit());
		trace.draw(loc, attackMeta.getDamager().getEyeLocation().getDirection(), 2,
				loc1 -> loc1.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, loc1, 0));
	}
}
