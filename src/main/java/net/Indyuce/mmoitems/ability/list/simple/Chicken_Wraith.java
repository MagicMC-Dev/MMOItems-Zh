package net.Indyuce.mmoitems.ability.list.simple;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import net.Indyuce.mmoitems.api.ItemAttackMetadata;
import net.Indyuce.mmoitems.api.util.TemporaryListener;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Egg;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class Chicken_Wraith extends SimpleAbility {
    public Chicken_Wraith() {
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
    public void whenCast(ItemAttackMetadata attack, SimpleAbilityMetadata ability) {
        double duration = ability.getModifier("duration") * 10;
        double force = ability.getModifier("force");
        double inaccuracy = ability.getModifier("inaccuracy");

        new BukkitRunnable() {
            final EggHandler handler = new EggHandler(ability.getModifier("damage"));
            int j = 0;

            public void run() {
                if (j++ > duration) {
                    handler.close(5 * 20);
                    cancel();
                    return;
                }

                Location loc = attack.getDamager().getEyeLocation();
                loc.setPitch((float) (loc.getPitch() + (random.nextDouble() - .5) * inaccuracy));
                loc.setYaw((float) (loc.getYaw() + (random.nextDouble() - .5) * inaccuracy));

                loc.getWorld().playSound(loc, Sound.ENTITY_CHICKEN_EGG, 1, 1);
                Egg egg = attack.getDamager().launchProjectile(Egg.class);
                egg.setVelocity(loc.getDirection().multiply(1.3 * force));

                handler.entities.add(egg.getEntityId());
            }
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}

	public static class EggHandler extends TemporaryListener {
		private final List<Integer> entities = new ArrayList<>();
		private final double damage;

		public EggHandler(double damage) {
			super(EntityDamageByEntityEvent.getHandlerList());

			this.damage = damage;
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void a(PlayerEggThrowEvent event) {
			if (entities.contains(event.getEgg().getEntityId())) {
				event.setHatching(false);
			}
		}

		@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
		public void b(EntityDamageByEntityEvent event) {
			if (entities.contains(event.getDamager().getEntityId()))
				event.setDamage(damage);
		}
	}
}
