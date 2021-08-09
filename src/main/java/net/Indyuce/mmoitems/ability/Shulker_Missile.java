package net.Indyuce.mmoitems.ability;

import io.lumine.mythic.lib.api.AttackResult;
import io.lumine.mythic.lib.api.DamageType;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.VectorAbilityResult;
import net.Indyuce.mmoitems.api.interaction.projectile.EntityData;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class Shulker_Missile extends Ability implements Listener {
	public Shulker_Missile() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
				CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("cooldown", 12);
		addModifier("damage", 5);
		addModifier("effect-duration", 5);
		addModifier("duration", 5);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new VectorAbilityResult(ability, stats.getPlayer(), target);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		double duration = ability.getModifier("duration");

		new BukkitRunnable() {
			double n = 0;

			public void run() {
				if (n++ > 3) {
					cancel();
					return;
				}

				Vector vec = ((VectorAbilityResult) ability).getTarget();
				stats.getPlayer().getWorld().playSound(stats.getPlayer().getLocation(), Sound.ENTITY_WITHER_SHOOT, 2, 2);
				ShulkerBullet shulkerBullet = (ShulkerBullet) stats.getPlayer().getWorld().spawnEntity(stats.getPlayer().getLocation().add(0, 1, 0),
						EntityType.SHULKER_BULLET);
				shulkerBullet.setShooter(stats.getPlayer());
				MMOItems.plugin.getEntities().registerCustomEntity(shulkerBullet, new ShulkerMissileEntityData(
						new AttackResult(ability.getModifier("damage"), DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE),
						ability.getModifier("effect-duration")));
				new BukkitRunnable() {
					double ti = 0;

					public void run() {
						if (shulkerBullet.isDead() || ti++ >= duration * 20) {
							shulkerBullet.remove();
							cancel();
						} else
							shulkerBullet.setVelocity(vec);
					}
				}.runTaskTimer(MMOItems.plugin, 0, 1);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 3);
	}

	@EventHandler
	public void a(EntityDamageByEntityEvent event) {
		if (event.getDamager() instanceof ShulkerBullet && event.getEntity() instanceof LivingEntity) {
			ShulkerBullet damager = (ShulkerBullet) event.getDamager();
			LivingEntity entity = (LivingEntity) event.getEntity();
			if (!MMOItems.plugin.getEntities().isCustomEntity(damager))
				return;

			if (!MMOUtils.canDamage(entity)) {
				event.setCancelled(true);
				return;
			}

			ShulkerMissileEntityData data = (ShulkerMissileEntityData) MMOItems.plugin.getEntities().getEntityData(damager);

			// Void spirit
			if (data.isWeaponAttack())
				((ItemAttackResult) data.result).applyEffects(data.stats, data.weapon, entity);

			event.setDamage(data.result.getDamage());

			new BukkitRunnable() {
				final Location loc = entity.getLocation();
				double y = 0;

				public void run() {

					// Potion effect should apply right after the damage with a 1 tick delay.
					if (y == 0) {
						entity.removePotionEffect(PotionEffectType.LEVITATION);
						entity.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, (int) (data.duration * 20), 0));
					}

					for (int j1 = 0; j1 < 3; j1++) {
						y += .04;
						for (int j = 0; j < 2; j++) {
							double xz = y * Math.PI * 1.3 + (j * Math.PI);
							loc.getWorld().spawnParticle(Particle.REDSTONE, loc.clone().add(Math.cos(xz), y, Math.sin(xz)), 1,
									new Particle.DustOptions(Color.MAROON, 1));
						}
					}
					if (y >= 2)
						cancel();
				}
			}.runTaskTimer(MMOItems.plugin, 0, 1);
		}
	}

	public static class ShulkerMissileEntityData implements EntityData {
		private final AttackResult result;
		private final double duration;

		@Nullable
		private final CachedStats stats;
		@Nullable
		private final NBTItem weapon;

		/**
		 * Used for the Shulker missile ability
		 *
		 * @param result   Attack result
		 * @param duration Duration of levitation effect in seconds
		 */
		public ShulkerMissileEntityData(AttackResult result, double duration) {
			this(result, duration, null, null);
		}

		/**
		 * Used for the void staff attack spirit (no levitation effect)
		 *
		 * @param result Attack result
		 * @param stats  Stats of player attacking
		 * @param weapon Item used for the attack
		 */
		public ShulkerMissileEntityData(ItemAttackResult result, CachedStats stats, NBTItem weapon) {
			this(result, 0, stats, weapon);
		}

		private ShulkerMissileEntityData(AttackResult result, double duration, CachedStats stats, NBTItem weapon) {
			this.result = result;
			this.duration = duration;
			this.stats = stats;
			this.weapon = weapon;
		}

		public boolean isWeaponAttack() {
			return result instanceof ItemAttackResult;
		}
	}
}
