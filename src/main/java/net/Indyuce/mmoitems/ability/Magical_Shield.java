package net.Indyuce.mmoitems.ability;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.SimpleAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import io.lumine.mythic.lib.version.VersionSound;

public class Magical_Shield extends Ability {
	public Magical_Shield() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
				CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("power", 40);
		addModifier("radius", 5);
		addModifier("duration", 5);
		addModifier("cooldown", 35);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new SimpleAbilityResult(ability, ((LivingEntity) stats.getPlayer()).isOnGround());
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		double duration = ability.getModifier("duration");
		double radiusSquared = Math.pow(ability.getModifier("radius"), 2);
		double power = ability.getModifier("power") / 100;

		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 3, 0);
		new MagicalShield(stats.getPlayer().getLocation().clone(), duration, radiusSquared, power);
	}

	public static class MagicalShield extends BukkitRunnable implements Listener {
		private final Location loc;
		private final double duration, radius, power;

		int ti = 0;

		public MagicalShield(Location loc, double duration, double radius, double power) {
			this.loc = loc;

			this.duration = duration;
			this.radius = radius;
			this.power = power;

			runTaskTimer(MMOItems.plugin, 0, 3);
			Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
		}

		private void close() {
			cancel();
			EntityDamageEvent.getHandlerList().unregister(this);
		}

		@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
		public void a(EntityDamageEvent event) {
			if (event.getEntity() instanceof Player && event.getEntity().getWorld().equals(loc.getWorld()) && event.getEntity().getLocation().distanceSquared(loc) < radius)
				event.setDamage(event.getDamage() * (1 - power));
		}

		@Override
		public void run() {
			ti++;
			if (ti > duration * 20. / 3.)
				close();

			for (double j = 0; j < Math.PI / 2; j += Math.PI / (28 + random.nextInt(5)))
				for (double i = 0; i < Math.PI * 2; i += Math.PI / (14 + random.nextInt(5)))
					loc.getWorld().spawnParticle(Particle.REDSTONE,
							loc.clone().add(2.5 * Math.cos(i + j) * Math.sin(j), 2.5 * Math.cos(j), 2.5 * Math.sin(i + j) * Math.sin(j)), 1,
							new Particle.DustOptions(Color.FUCHSIA, 1));
		}
	}
}
