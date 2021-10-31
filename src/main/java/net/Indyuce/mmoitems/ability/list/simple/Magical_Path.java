package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.version.VersionSound;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class Magical_Path extends SimpleAbility {
    public Magical_Path() {
        super();

        addModifier("duration", 3);
        addModifier("cooldown", 15);
        addModifier("mana", 0);
        addModifier("stamina", 0);
    }

    @Override
    public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
        attack.getPlayer().setAllowFlight(true);
        attack.getPlayer().setFlying(true);
        attack.getPlayer().setVelocity(attack.getPlayer().getVelocity().setY(.5));
        attack.getPlayer().getWorld().playSound(attack.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 1, 1);

        new MagicalPathHandler(attack.getPlayer(), ability.getModifier("duration"));
    }

	public static class MagicalPathHandler extends BukkitRunnable implements Listener {
		private final Player player;
		private final long duration;

		/*
		 * when true, the next fall damage is negated
		 */
		private boolean safe = true;

		private int j = 0;

		public MagicalPathHandler(Player player, double duration) {
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
				player.getWorld().playSound(player.getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 1, 1);
				player.setAllowFlight(false);
				cancel();
				return;
			}

			player.getWorld().spawnParticle(Particle.SPELL, player.getLocation(), 8, .5, 0, .5, .1);
			player.getWorld().spawnParticle(Particle.SPELL_INSTANT, player.getLocation(), 16, .5, 0, .5, .1);
		}
	}
}
