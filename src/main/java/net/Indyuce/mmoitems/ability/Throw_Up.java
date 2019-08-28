package net.Indyuce.mmoitems.ability;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.util.NoInteractItemEntity;
import net.Indyuce.mmoitems.manager.DamageManager.DamageType;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.nms.ItemTag;

public class Throw_Up extends Ability implements Listener {
	public Throw_Up() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 2.5);
		addModifier("damage", 2);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		double duration = data.getModifier("duration") * 10;
		double dps = data.getModifier("damage") / 2;

		new BukkitRunnable() {
			int j = 0;

			public void run() {
				j++;
				if (j > duration)
					cancel();

				Location loc = stats.getPlayer().getEyeLocation();
				loc.setPitch((float) (loc.getPitch() + (random.nextDouble() - .5) * 30));
				loc.setYaw((float) (loc.getYaw() + (random.nextDouble() - .5) * 30));

				if (j % 5 == 0)
					for (Entity ent : MMOUtils.getNearbyChunkEntities(loc))
						if (ent.getLocation().distanceSquared(loc) < 40)
							if (stats.getPlayer().getEyeLocation().getDirection().angle(ent.getLocation().toVector().subtract(stats.getPlayer().getLocation().toVector())) < Math.PI / 6)
								if (MMOUtils.canDamage(stats.getPlayer(), ent))
									MMOItems.plugin.getDamage().damage(stats, (LivingEntity) ent, dps, DamageType.MAGIC);

				loc.getWorld().playSound(loc, Sound.ENTITY_ZOMBIE_HURT, 1, 1);

				NoInteractItemEntity item = new NoInteractItemEntity(stats.getPlayer().getLocation().add(0, 1.2, 0), MMOItems.plugin.getNMS().getNBTItem(new ItemStack(Material.ROTTEN_FLESH)).addTag(new ItemTag("noStack", random.nextInt(1000))).toItem());
				Bukkit.getScheduler().scheduleSyncDelayedTask(MMOItems.plugin, () -> item.close(), 40);
				item.getEntity().setVelocity(loc.getDirection().multiply(.8));
				stats.getPlayer().getWorld().spawnParticle(Particle.SMOKE_LARGE, stats.getPlayer().getLocation().add(0, 1.2, 0), 0, loc.getDirection().getX(), loc.getDirection().getY(), loc.getDirection().getZ(), 1);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}
}
