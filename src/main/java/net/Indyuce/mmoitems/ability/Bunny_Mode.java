package net.Indyuce.mmoitems.ability;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.SimpleAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.api.util.TemporaryListener;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.version.VersionSound;
import org.jetbrains.annotations.NotNull;

public class Bunny_Mode extends Ability {
	public Bunny_Mode() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
				CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 20);
		addModifier("jump-force", 1);
		addModifier("cooldown", 50);
		addModifier("speed", 1);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new SimpleAbilityResult(ability);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		double duration = ability.getModifier("duration") * 20;
		double y = ability.getModifier("jump-force");
		double xz = ability.getModifier("speed");

		new BukkitRunnable() {
			int j = 0;
			final BunnyHandler handler = new BunnyHandler(stats.getPlayer(), duration);

			public void run() {
				if (j++ > duration) {
					handler.close(3 * 20);
					cancel();
					return;
				}

				if (stats.getPlayer().getLocation().add(0, -.5, 0).getBlock().getType().isSolid()) {
					stats.getPlayer()
							.setVelocity(stats.getPlayer().getEyeLocation().getDirection().setY(0).normalize().multiply(.8 * xz).setY(0.5 * y / xz));
					stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 2, 1);
					for (double a = 0; a < Math.PI * 2; a += Math.PI / 12)
						stats.getPlayer().getWorld().spawnParticle(Particle.CLOUD, stats.getPlayer().getLocation(), 0, Math.cos(a), 0, Math.sin(a),
								.2);
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);

	}

	public static class BunnyHandler extends TemporaryListener {
		private final Player player;

		public BunnyHandler(Player player, double duration) {
			super(EntityDamageEvent.getHandlerList());

			this.player = player;

			Bukkit.getScheduler().runTaskLater(MMOItems.plugin, (Runnable) this::close, (long) (duration * 20));
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void a(EntityDamageEvent event) {
			if (event.getEntity().equals(player) && event.getCause() == DamageCause.FALL)
				event.setCancelled(true);
		}
	}
}
