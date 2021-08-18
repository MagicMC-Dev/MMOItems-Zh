package net.Indyuce.mmoitems.listener;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

// TODO refactor this shit
public class ElementListener implements Listener {
	private static final Map<Integer, Long> WATER_WEAKNESS = new HashMap<>();

	private static final long WATER_WEAKNESS_DURATION = 1000 * 6;
	private static final double WATER_WEAKNESS_DAMAGE_INCREASE = 0.3;

	public static void weaken(Entity entity) {
		WATER_WEAKNESS.put(entity.getEntityId(), System.currentTimeMillis());
	}

	boolean isWeakened(Entity entity) {
		return WATER_WEAKNESS.containsKey(entity.getEntityId()) && WATER_WEAKNESS.get(entity.getEntityId()) + WATER_WEAKNESS_DURATION > System.currentTimeMillis();
	}

	void flush() {
		WATER_WEAKNESS.entrySet().removeIf(integerLongEntry -> integerLongEntry.getValue() + WATER_WEAKNESS_DURATION < System.currentTimeMillis());
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void a(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		if (isWeakened(entity)) {
			event.setDamage(event.getDamage() * (1 + WATER_WEAKNESS_DAMAGE_INCREASE));
			entity.getWorld().spawnParticle(Particle.WATER_SPLASH, event.getEntity().getLocation().add(0, entity.getHeight() / 2, 0), 16, .3, .3, .3, 0);
		}
	}
}
