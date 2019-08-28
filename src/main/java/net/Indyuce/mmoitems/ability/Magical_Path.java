package net.Indyuce.mmoitems.ability;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.VersionSound;

public class Magical_Path extends Ability implements Listener {
	private Map<UUID, Long> fallDamage = new HashMap<UUID, Long>();

	public Magical_Path() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 3);
		addModifier("cooldown", 15);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		double duration = data.getModifier("duration");

		stats.getPlayer().setAllowFlight(true);
		stats.getPlayer().setFlying(true);
		stats.getPlayer().setVelocity(stats.getPlayer().getVelocity().setY(.5));
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 1, 1);

		new BukkitRunnable() {
			int j = 0;

			public void run() {
				if (j++ > duration * 10) {
					stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 1, 1);
					stats.getPlayer().setAllowFlight(false);
					cancel();
					return;
				}

				stats.getPlayer().getWorld().spawnParticle(Particle.SPELL, stats.getPlayer().getLocation(), 8, .5, 0, .5, .1);
				stats.getPlayer().getWorld().spawnParticle(Particle.SPELL_INSTANT, stats.getPlayer().getLocation(), 16, .5, 0, .5, .1);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 2);

		fallDamage.put(stats.getPlayer().getUniqueId(), (long) (System.currentTimeMillis() + duration * 3000));
	}

	@EventHandler
	public void a(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player) || event.getCause() != DamageCause.FALL)
			return;

		Player player = (Player) event.getEntity();
		if (!fallDamage.containsKey(player.getUniqueId()))
			return;

		if (fallDamage.get(player.getUniqueId()) > System.currentTimeMillis()) {
			player.getWorld().spawnParticle(Particle.SPELL, player.getLocation(), 16, .5, 0, .5, .1);
			player.getWorld().spawnParticle(Particle.SPELL_INSTANT, player.getLocation(), 32, .5, 0, .5, .1);
			player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ENDERMAN_HURT.toSound(), 1, 2);
			event.setCancelled(true);
			return;
		}

		// clear player from map not to overload memory
		fallDamage.remove(player.getUniqueId());
	}
}
