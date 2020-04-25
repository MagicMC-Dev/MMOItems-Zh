package net.Indyuce.mmoitems.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemAttackResult;
import net.Indyuce.mmoitems.api.ability.Ability;
import net.Indyuce.mmoitems.api.ability.AbilityResult;
import net.Indyuce.mmoitems.api.ability.SimpleAbilityResult;
import net.Indyuce.mmoitems.api.player.PlayerStats.CachedStats;
import net.Indyuce.mmoitems.api.util.TemporaryListener;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Blizzard extends Ability {
	public Blizzard() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK,
				CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 2.5);
		addModifier("damage", 2);
		addModifier("inaccuracy", 10);
		addModifier("force", 1);
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
		double force = ability.getModifier("force");
		double inaccuracy = ability.getModifier("inaccuracy");

		new BukkitRunnable() {
			int j = 0;
			SnowballThrower handler = new SnowballThrower(ability.getModifier("damage"));

			public void run() {
				if (j++ > duration) {
					handler.close(5 * 20);
					cancel();
					return;
				}

				Location loc = stats.getPlayer().getEyeLocation();
				loc.setPitch((float) (loc.getPitch() + (random.nextDouble() - .5) * inaccuracy));
				loc.setYaw((float) (loc.getYaw() + (random.nextDouble() - .5) * inaccuracy));

				loc.getWorld().playSound(loc, Sound.ENTITY_SNOWBALL_THROW, 1, 1);
				Snowball snowball = stats.getPlayer().launchProjectile(Snowball.class);
				snowball.setVelocity(loc.getDirection().multiply(1.3 * force));
				handler.entities.add(snowball.getUniqueId());
			}
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}

	public class SnowballThrower extends TemporaryListener {
		private final List<UUID> entities = new ArrayList<>();
		private final double damage;

		public SnowballThrower(double damage) {
			super(EntityDamageByEntityEvent.getHandlerList());

			this.damage = damage;
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void a(EntityDamageByEntityEvent event) {
			if (entities.contains(event.getDamager().getUniqueId()))
				event.setDamage(damage);
		}
	}
}
