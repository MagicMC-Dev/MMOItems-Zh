package net.Indyuce.mmoitems.api.interaction.weapon.untargeted;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.NBTItem;
import net.Indyuce.mmoitems.api.player.PlayerData.CooldownType;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.player.damage.AttackResult;
import net.Indyuce.mmoitems.api.player.damage.AttackResult.DamageType;
import net.Indyuce.mmoitems.api.util.MMORayTraceResult;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.version.VersionSound;

public class Whip extends UntargetedWeapon {
	public Whip(Player player, NBTItem item, Type type) {
		super(player, item, type, WeaponType.LEFT_CLICK);
	}

	@Override
	public void untargetedAttackEffects() {

		TemporaryStats stats = getPlayerData().getStats().newTemporary();
		if (!hasEnoughResources(1 / getValue(stats.getStat(ItemStat.ATTACK_SPEED), MMOItems.plugin.getConfig().getDouble("default.attack-speed")), CooldownType.ATTACK, false))
			return;

		double attackDamage = getValue(stats.getStat(ItemStat.ATTACK_DAMAGE), 1);
		double range = getValue(getNBTItem().getStat(ItemStat.RANGE), MMOItems.plugin.getConfig().getDouble("default.range"));

		double a = Math.toRadians(getPlayer().getEyeLocation().getYaw() + 160);
		Location loc = getPlayer().getEyeLocation().add(new Vector(Math.cos(a), 0, Math.sin(a)).multiply(.5));

		MMORayTraceResult trace = MMOItems.plugin.getVersion().getWrapper().rayTrace(stats.getPlayer(), range);
		if (trace.hasHit())
			new AttackResult(attackDamage, DamageType.WEAPON, DamageType.PROJECTILE, DamageType.PHYSICAL).applyEffectsAndDamage(stats, getNBTItem(), trace.getHit());
		trace.draw(loc, getPlayer().getEyeLocation().getDirection(), 2, (tick) -> tick.getWorld().spawnParticle(Particle.CRIT, tick, 0, .1, .1, .1, 0));
		getPlayer().getWorld().playSound(getPlayer().getLocation(), VersionSound.ENTITY_FIREWORK_ROCKET_BLAST.toSound(), 1, 2);
	}
}
