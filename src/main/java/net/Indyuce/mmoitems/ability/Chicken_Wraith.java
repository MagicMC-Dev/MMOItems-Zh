package net.Indyuce.mmoitems.ability;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Ability;
import net.Indyuce.mmoitems.api.player.PlayerStats.TemporaryStats;
import net.Indyuce.mmoitems.api.player.damage.AttackResult;
import net.Indyuce.mmoitems.stat.data.AbilityData;

public class Chicken_Wraith extends Ability implements Listener {
	public Chicken_Wraith() {
		super(CastingMode.ON_HIT, CastingMode.WHEN_HIT, CastingMode.LEFT_CLICK, CastingMode.RIGHT_CLICK, CastingMode.SHIFT_LEFT_CLICK, CastingMode.SHIFT_RIGHT_CLICK);

		addModifier("duration", 2.5);
		addModifier("damage", 2);
		addModifier("inaccuracy", 10);
		addModifier("force", 1);
		addModifier("cooldown", 10);
		addModifier("mana", 0);
		addModifier("stamina", 0);
	}

	@Override
	public void whenCast(TemporaryStats stats, LivingEntity target, AbilityData data, AttackResult result) {
		double duration = data.getModifier("duration") * 10;
		double force = data.getModifier("force");
		double inaccuracy = data.getModifier("inaccuracy");

		new BukkitRunnable() {
			int j = 0;
			double damage = data.getModifier("damage");

			public void run() {
				j++;
				if (j > duration)
					cancel();

				Location loc = stats.getPlayer().getEyeLocation();
				loc.setPitch((float) (loc.getPitch() + (random.nextDouble() - .5) * inaccuracy));
				loc.setYaw((float) (loc.getYaw() + (random.nextDouble() - .5) * inaccuracy));

				loc.getWorld().playSound(loc, Sound.ENTITY_CHICKEN_EGG, 1, 1);
				Egg snowball = stats.getPlayer().launchProjectile(Egg.class);
				snowball.setVelocity(loc.getDirection().multiply(1.3 * force));
				MMOItems.plugin.getEntities().registerCustomEntity(snowball, damage);
			}
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}

	@EventHandler
	public void a(EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Egg))
			return;

		Egg egg = (Egg) event.getDamager();
		if (MMOItems.plugin.getEntities().isCustomEntity(egg))
			event.setDamage((double) MMOItems.plugin.getEntities().getEntityData(egg)[0]);
	}
}
