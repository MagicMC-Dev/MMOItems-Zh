package net.Indyuce.mmoitems.ability;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.SimpleAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.api.util.TemporaryListener;
import net.Indyuce.mmoitems.stat.data.AbilityData;
import net.mmogroup.mmolib.api.AttackResult;
import net.mmogroup.mmolib.api.DamageType;

public class Explosive_Turkey extends Ability implements Listener {
	public Explosive_Turkey() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
				CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("damage", 6);
		addModifier("radius", 4);
		addModifier("duration", 4);
		addModifier("knockback", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public AbilityResult whenRan(CachedStats stats, LivingEntity target, AbilityData ability, ItemAttackResult result) {
		return new SimpleAbilityResult(ability);
	}

	@Override
	public void whenCast(CachedStats stats, AbilityResult ability, ItemAttackResult result) {
		double duration = ability.getModifier("duration") * 10;
		double damage = ability.getModifier("damage");
		double radiusSquared = Math.pow(ability.getModifier("radius"), 2);
		double knockback = ability.getModifier("knockback");

		Vector vec = stats.getPlayer().getEyeLocation().getDirection().clone().multiply(.6);

		Chicken chicken = (Chicken) stats.getPlayer().getWorld().spawnEntity(stats.getPlayer().getLocation().add(0, 1.3, 0).add(vec),
				EntityType.CHICKEN);
		ChickenHandler chickenHandler = new ChickenHandler(chicken);
		chicken.setInvulnerable(true);
		chicken.setSilent(true);

		/*
		 * Sets the health to 2048 (Default max Spigot value) which stops the
		 * bug where you can kill the chicken for a brief few ticks after it
		 * spawns in!
		 */
		chicken.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(2048);
		chicken.setHealth(2048);

		/*
		 * when items are moving through the air, they loose a percent of their
		 * velocity proportionally to their coordinates in each axis. this means
		 * that if the trajectory is not affected, the ratio of x/y will always
		 * be the same. check for any change of that ratio to check for a
		 * trajectory change
		 */
		chicken.setVelocity(vec);

		final double trajRatio = chicken.getVelocity().getX() / chicken.getVelocity().getZ();

		new BukkitRunnable() {
			int ti = 0;

			public void run() {
				if (ti++ > duration || chicken.isDead()) {
					chickenHandler.close();
					cancel();
					return;
				}

				chicken.setVelocity(vec);
				if (ti % 4 == 0)
					chicken.getWorld().playSound(chicken.getLocation(), Sound.ENTITY_CHICKEN_HURT, 2, 1);
				chicken.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, chicken.getLocation().add(0, .3, 0), 0);
				chicken.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, chicken.getLocation().add(0, .3, 0), 1, 0, 0, 0, .05);
				double currentTrajRatio = chicken.getVelocity().getX() / chicken.getVelocity().getZ();
				if (chicken.isOnGround() || Math.abs(trajRatio - currentTrajRatio) > .1) {

					chickenHandler.close();
					cancel();

					chicken.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, chicken.getLocation().add(0, .3, 0), 128, 0, 0, 0, .25);
					chicken.getWorld().spawnParticle(Particle.EXPLOSION_NORMAL, chicken.getLocation().add(0, .3, 0), 24, 0, 0, 0, .25);
					chicken.getWorld().playSound(chicken.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2, 1.5f);
					for (Entity entity : MMOUtils.getNearbyChunkEntities(chicken.getLocation()))
						if (!entity.isDead() && entity.getLocation().distanceSquared(chicken.getLocation()) < radiusSquared
								&& MMOUtils.canDamage(stats.getPlayer(), entity)) {
							new AttackResult(damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE).damage(stats.getPlayer(),
									(LivingEntity) entity);
							entity.setVelocity(entity.getLocation().toVector().subtract(chicken.getLocation().toVector()).multiply(.1 * knockback)
									.setY(.4 * knockback));
						}
					return;
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}

	/*
	 * this fixes an issue where chickens sometimes drop
	 */
	public class ChickenHandler extends TemporaryListener {
		private final Chicken chicken;

		public ChickenHandler(Chicken chicken) {
			super(EntityDeathEvent.getHandlerList());

			this.chicken = chicken;
		}

		/*
		 * make sure the chicken is ALWAYS killed, this class really uses
		 * overkill methods but there are plently issues with chickens remaining
		 */
		@Override
		public void close() {
			chicken.remove();
			super.close();
		}

		@EventHandler
		public void a(EntityDeathEvent event) {
			if (event.getEntity().equals(chicken)) {
				event.getDrops().clear();
				event.setDroppedExp(0);
			}
		}
	}
}
