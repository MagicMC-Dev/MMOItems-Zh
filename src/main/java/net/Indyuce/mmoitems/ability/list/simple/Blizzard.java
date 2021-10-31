package net.Indyuce.mmoitems.ability.list.simple;

import io.lumine.mythic.lib.api.util.TemporaryListener;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.ability.SimpleAbility;
import net.Indyuce.mmoitems.ability.metadata.SimpleAbilityMetadata;
import io.lumine.mythic.lib.damage.AttackMetadata;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Blizzard extends SimpleAbility {
    public Blizzard() {
        super();

        addModifier("duration", 2.5);
        addModifier("damage", 2);
        addModifier("inaccuracy", 10);
        addModifier("force", 1);
        addModifier("cooldown", 10);
        addModifier("mana", 0);
		addModifier("stamina", 0);
	}

    @Override
    public void whenCast(AttackMetadata attack, SimpleAbilityMetadata ability) {
        double duration = ability.getModifier("duration") * 10;
        double force = ability.getModifier("force");
        double inaccuracy = ability.getModifier("inaccuracy");

        new BukkitRunnable() {
            final SnowballThrower handler = new SnowballThrower(ability.getModifier("damage"));
            int j = 0;

            public void run() {
                if (j++ > duration) {
                    handler.close(5 * 20);
                    cancel();
                    return;
                }

                Location loc = attack.getPlayer().getEyeLocation();
                loc.setPitch((float) (loc.getPitch() + (random.nextDouble() - .5) * inaccuracy));
                loc.setYaw((float) (loc.getYaw() + (random.nextDouble() - .5) * inaccuracy));

                loc.getWorld().playSound(loc, Sound.ENTITY_SNOWBALL_THROW, 1, 1);
                Snowball snowball = attack.getPlayer().launchProjectile(Snowball.class);
                snowball.setVelocity(loc.getDirection().multiply(1.3 * force));
                handler.entities.add(snowball.getUniqueId());
            }
		}.runTaskTimer(MMOItems.plugin, 0, 2);
	}

	public static class SnowballThrower extends TemporaryListener {
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
