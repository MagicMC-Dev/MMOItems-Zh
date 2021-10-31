package net.Indyuce.mmoitems.ability.list.vector;

import io.lumine.mythic.lib.api.util.TemporaryListener;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamageType;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.MMOUtils;
import net.Indyuce.mmoitems.ability.VectorAbility;
import net.Indyuce.mmoitems.ability.metadata.VectorAbilityMetadata;
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

public class Explosive_Turkey extends VectorAbility implements Listener {
	public Explosive_Turkey() {
		super();

		addModifier("damage", 6);
		addModifier("radius", 4);
		addModifier("duration", 4);
		addModifier("knockback", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(AttackMetadata attack, VectorAbilityMetadata ability) {
		double duration = ability.getModifier("duration") * 10;
		double damage = ability.getModifier("damage");
		double radiusSquared = Math.pow(ability.getModifier("radius"), 2);
		double knockback = ability.getModifier("knockback");

		Vector vec = ability.getTarget().normalize().multiply(.6);

		Chicken chicken = (Chicken) attack.getPlayer().getWorld().spawnEntity(attack.getPlayer().getLocation().add(0, 1.3, 0).add(vec),
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
		 * When items are moving through the air, they loose a percent of their
		 * velocity proportionally to their coordinates in each axis. This means
		 * that if the trajectory is not affected, the ratio of x/y will always
		 * be the same. Check for any change of that ratio to check for a
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
								&& MMOUtils.canTarget(attack.getPlayer(), entity)) {
							new AttackMetadata(new DamageMetadata(damage, DamageType.SKILL, DamageType.MAGIC, DamageType.PROJECTILE), attack.getStats()).damage((LivingEntity) entity);
							entity.setVelocity(entity.getLocation().toVector().subtract(chicken.getLocation().toVector()).multiply(.1 * knockback)
									.setY(.4 * knockback));
						}
				}
			}
		}.runTaskTimer(MMOItems.plugin, 0, 1);
	}

	/**
	 * This fixes an issue where chickens sometimes drop
	 */
	public static class ChickenHandler extends TemporaryListener {
		private final Chicken chicken;

		public ChickenHandler(Chicken chicken) {
			super(EntityDeathEvent.getHandlerList());

			this.chicken = chicken;
		}

		/*
		 * Make sure the chicken is ALWAYS killed, this class really uses
		 * overkill methods but there are plently issues with chickens remaining
		 */
		@Override
		public boolean close() {
		    boolean b = super.close();
		    if (b)
		        chicken.remove();
		    return b;
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
