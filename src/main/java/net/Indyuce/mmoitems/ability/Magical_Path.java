package net.Indyuce.mmoitems.ability;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.VersionSound;

public class Magical_Path extends Ability {
	public Magical_Path() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK,
				CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 3);
		addModifier("cooldown", 15);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		stats.getPlayer().setAllowFlight(true);
		stats.getPlayer().setFlying(true);
		stats.getPlayer().setVelocity(stats.getPlayer().getVelocity().setY(.5));
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(),
				VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 1, 1);

		new ShadowVeilHandler(stats.getPlayer(), data.getModifier("duration"));
	}

	public class ShadowVeilHandler extends BukkitRunnable implements Listener {
		private final Player player;
		private final long duration;

		/*
		 * when true, the next fall damage is negated
		 */
		private boolean safe = true;

		private int j = 0;

		public ShadowVeilHandler(Player player, double duration) {
			this.player = player;
			this.duration = (long) (duration * 10);

			runTaskTimer(MMOItems.plugin, 0, 2);
			Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
		}

		public void close() {
			player.setAllowFlight(false);
			HandlerList.unregisterAll(this);
			cancel();
		}

		@EventHandler(priority = EventPriority.LOW)
		public void a(EntityDamageEvent event) {
			if (safe && event.getEntity().equals(player) && event.getCause() == DamageCause.FALL) {
				event.setCancelled(true);
				safe = false;

				player.getWorld().spawnParticle(Particle.SPELL, player.getLocation(), 8, .35, 0, .35, .08);
				player.getWorld().spawnParticle(Particle.SPELL_INSTANT, player.getLocation(), 16, .35, 0, .35, .08);
				player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ENDERMAN_HURT.toSound(), 1, 2);
			}
		}

		@EventHandler
		public void b(PlayerQuitEvent event) {
			close();
		}

		@Override
		public void run() {

			if (j++ > duration) {
				player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 1,
						1);
				player.setAllowFlight(false);
				cancel();
				return;
			}

			player.getWorld().spawnParticle(Particle.SPELL, player.getLocation(), 8, .5, 0, .5, .1);
			player.getWorld().spawnParticle(Particle.SPELL_INSTANT, player.getLocation(), 16, .5, 0, .5, .1);
		}
	}
}
