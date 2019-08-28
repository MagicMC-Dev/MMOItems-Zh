package net.Indyuce.mmoitems.ability;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.manager.DamageManager.DamageType;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.VersionSound;

public class Light_Dash extends Ability {
	public Light_Dash() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 3);
		addModifier("cooldown", 10);
		addModifier("length", 1);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		double damage1 = data.getModifier("damage");
		double length = data.getModifier("length");

		new BukkitRunnable() {
			int j = 0;
			Vector vec = stats.getPlayer().getEyeLocation().getDirection();
			List<Integer> hit = new ArrayList<>();

			public void run() {
				if (j++ > 10 * Math.min(10, length))
					cancel();

				stats.getPlayer().setVelocity(vec);
				stats.getPlayer().getWorld().spawnParticle(Particle.SMOKE_LARGE, stats.getPlayer().getLocation().add(0, 1, 0), 0);
				stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), VersionSound.ENTITY_ENDER_DRAGON_FLAP.toSound(), 1, 2);
				for (Entity entity : stats.getPlayer().getNearbyEntities(1, 1, 1))
					if (!hit.contains(entity.getEntityId()) && MMOUtils.canDamage(stats.getPlayer(), entity)) {
						hit.add(entity.getEntityId());
						MMOItems.plugin.getDamage().damage(stats, (LivingEntity) entity, damage1, DamageType.PHYSICAL);
					}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}
}
