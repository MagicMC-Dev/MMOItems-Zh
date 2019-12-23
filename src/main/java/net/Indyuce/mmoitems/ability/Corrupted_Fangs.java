package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.DamageType;

public class Corrupted_Fangs extends Ability implements Listener {
	public Corrupted_Fangs() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 5);
		addModifier("cooldown", 12);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(CachedStats stats, LivingEntity target, AbilityData data, ItemAttackResult result) {
		double damage1 = data.getModifier("damage");

		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
		new BukkitRunnable() {
			Vector vec = getTargetDirection(stats.getPlayer(), target).setY(0).multiply(2);
			Location loc = stats.getPlayer().getLocation();
			double ti = 0;

			public void run() {
				ti += 2;
				loc.add(vec);

				EvokerFangs evokerFangs = (EvokerFangs) stats.getPlayer().getWorld().spawnEntity(loc, EntityType.EVOKER_FANGS);
				MMOItems.plugin.getEntities().registerCustomEntity(evokerFangs, stats, damage1);

				if (ti > 12)
					cancel();
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}

	@EventHandler
	public void a(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof EvokerFangs && event.getEntity() instanceof LivingEntity) {
			EvokerFangs damager = (EvokerFangs) event.getDamager();
			if (!MMOItems.plugin.getEntities().isCustomEntity(damager))
				return;

			event.setCancelled(true);
			Object[] data = MMOItems.plugin.getEntities().getEntityData(damager);
			CachedStats stats = (CachedStats) data[0];
			if (MMOUtils.canDamage(stats.getPlayer(), event.getEntity()))
				new ItemAttackResult((double) data[1], DamageType.SKILL, DamageType.MAGICAL).applyEffectsAndDamage(stats, null, (LivingEntity) event.getEntity());
		}
	}
}
