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
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.player.damage.AttackResult;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.VersionSound;

public class Bunny_Mode extends Ability implements Listener {
	private Map<UUID, Long> fallDamage = new HashMap<UUID, Long>();

	public Bunny_Mode() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 20);
		addModifier("jump-force", 1);
		addModifier("cooldown", 50);
		addModifier("speed", 1);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		double duration = data.getModifier("duration") * 20;
		double y = data.getModifier("jump-force");
		double xz = data.getModifier("speed");

		new BukkitRunnable() {
			int j = 0;

			public void run() {
				j++;
				if (j > duration)
					cancel();

				if (stats.getPlayer().getLocation().add(0, -.5, 0).getBlock().getType().isSolid()) {
					stats.getPlayer().setVelocity(stats.getPlayer().getEyeLocation().getDirection().setY(0).normalize().multiply(.8 * xz).setY(0.5 * y / xz));
					stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 2, 1);
					for (double a = 0; a < Math.PI * 2; a += Math.PI / 12)
						stats.getPlayer().getWorld().spawnParticle(Particle.CLOUD, stats.getPlayer().getLocation(), 0, Math.cos(a), 0, Math.sin(a), .2);
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);

		fallDamage.put(stats.getPlayer().getUniqueId(), (long) (System.currentTimeMillis() + duration * 100 + 3000));
	}

	@EventHandler
	public void a(EntityDamageEvent event) {
		if (!(event.getEntity() instanceof Player) || event.getCause() != DamageCause.FALL)
			return;

		Player player = (Player) event.getEntity();
		if (!fallDamage.containsKey(player.getUniqueId()))
			return;

		if (fallDamage.get(player.getUniqueId()) > System.currentTimeMillis()) {
			event.setCancelled(true);
			return;
		}

		// clear player from map not to overload memory
		fallDamage.remove(player.getUniqueId());
	}
}
