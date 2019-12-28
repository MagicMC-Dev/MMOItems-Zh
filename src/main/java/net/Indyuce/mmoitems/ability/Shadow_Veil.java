package net.Indyuce.mmoitems.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.SimpleAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.version.VersionSound;

public class Shadow_Veil extends Ability implements Listener {
	public final List<UUID> shadowVeil = new ArrayList<>();

	public Shadow_Veil() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 35);
		addModifier("duration", 5);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new SimpleAbilityResult(ability);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		double duration = ability.getModifier("duration");

		shadowVeil.add(stats.getPlayer().getUniqueId());
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 3, 0);
		for (Player online : Bukkit.getOnlinePlayers())
			online.hidePlayer(MMOItems.plugin, stats.getPlayer());
		for (Mob serverEntities : stats.getPlayer().getWorld().getEntitiesByClass(Mob.class))
			if (serverEntities.getTarget() != null && serverEntities.getTarget().equals(stats.getPlayer()))
				serverEntities.setTarget(null);
		new BukkitRunnable() {
			double ti = 0;
			double y = 0;
			Location loc = stats.getPlayer().getLocation();

			public void run() {
				ti++;
				if (ti > duration * 20) {
					for (Player online : Bukkit.getOnlinePlayers())
						online.showPlayer(MMOItems.plugin, stats.getPlayer());

					shadowVeil.remove(stats.getPlayer().getUniqueId());
					stats.getPlayer().getWorld().spawnParticle(Particle.SMOKE_LARGE, stats.getPlayer().getLocation().add(0, 1, 0), 32, 0, 0, 0, .13);
					stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 3, 0);
					cancel();
					return;
				}

				if (!shadowVeil.contains(stats.getPlayer().getUniqueId())) {
					for (Player online : Bukkit.getOnlinePlayers())
						online.showPlayer(MMOItems.plugin, stats.getPlayer());

					stats.getPlayer().getWorld().spawnParticle(Particle.SMOKE_LARGE, stats.getPlayer().getLocation().add(0, 1, 0), 32, 0, 0, 0, .13);
					stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDERMAN_TELEPORT.toSound(), 3, 0);
					cancel();
				}

				if (y < 4)
					for (int j1 = 0; j1 < 5; j1++) {
						y += .04;
						for (int j = 0; j < 4; j++) {
							double xz = y * Math.PI * .8 + (j * Math.PI / 2);
							stats.getPlayer().getWorld().spawnParticle(Particle.SMOKE_LARGE, loc.clone().add(Math.cos(xz) * 2.5, y, Math.sin(xz) * 2.5), 0);
						}
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}

	@EventHandler
	public void a(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player))
			return;

		Player player = (Player) event.getDamager();
		if (shadowVeil.contains(player.getUniqueId()))
			shadowVeil.remove(player.getUniqueId());
	}

	@EventHandler
	public void b(EntityTargetEvent event) {
		if (!(event.getTarget() instanceof Player))
			return;

		Player player = (Player) event.getTarget();
		if (shadowVeil.contains(player.getUniqueId()))
			event.setCancelled(true);
	}
}
