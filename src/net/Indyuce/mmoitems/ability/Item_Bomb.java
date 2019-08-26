package net.Indyuce.mmoitems.ability;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.AttackResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.util.NoInteractItemEntity;
import net.Indyuce.mmoitems.manager.DamageManager.DamageType;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.Indyuce.mmoitems.version.VersionSound;

public class Item_Bomb extends Ability implements Listener {
	public Item_Bomb() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 7);
		addModifier("radius", 6);
		addModifier("slow-duration", 4);
		addModifier("slow-amplifier", 1);
		addModifier("cooldown", 15);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		ItemStack itemStack = stats.getPlayer().getInventory().getItemInMainHand().clone();
		if (itemStack == null || itemStack.getType() == Material.AIR) {
			result.setSuccessful(false);
			return;
		}

		final NoInteractItemEntity item = new NoInteractItemEntity(stats.getPlayer().getLocation().add(0, 1.2, 0), itemStack);
		item.getEntity().setVelocity(getTargetDirection(stats.getPlayer(), target).multiply(1.3));
		stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_SNOWBALL_THROW, 2, 0);

		new BukkitRunnable() {
			int j = 0;

			public void run() {
				if (j++ > 40) {
					double radius = data.getModifier("radius");
					double damage = data.getModifier("damage");
					double slowDuration = data.getModifier("slow-duration");
					double slowAmplifier = data.getModifier("slow-amplifier");

					for (Entity entity : item.getEntity().getNearbyEntities(radius, radius, radius))
						if (MMOUtils.canDamage(stats.getPlayer(), entity)) {
							LivingEntity living = (LivingEntity) entity;
							MMOItems.plugin.getDamage().damage(stats, living, damage, DamageType.MAGIC);
							living.removePotionEffect(PotionEffectType.SLOW);
							living.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (int) (slowDuration * 20), (int) slowAmplifier));
						}

					item.getEntity().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, item.getEntity().getLocation(), 24, 2, 2, 2, 0);
					item.getEntity().getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, item.getEntity().getLocation(), 48, 0, 0, 0, .2);
					item.getEntity().getWorld().playSound(item.getEntity().getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 3, 0);

					item.close();
					cancel();
					return;
				}

				item.getEntity().getWorld().spawnParticle(Particle.SMOKE_LARGE, item.getEntity().getLocation().add(0, .2, 0), 0);
				item.getEntity().getWorld().spawnParticle(Particle.FIREWORKS_SPARK, item.getEntity().getLocation().add(0, .2, 0), 1, 0, 0, 0, .1);
				item.getEntity().getWorld().playSound(item.getEntity().getLocation(), VersionSound.BLOCK_NOTE_BLOCK_HAT.toSound(), 2, (float) (.5 + (j / 40. * 1.5)));
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}
}
