package net.Indyuce.mmoitems.listener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class ElementListener implements Listener {
	private static Map<Integer, Long> waterWeakness = new HashMap<>();

	private static final long waterWeaknessDuration = 1000 * 6;
	private static final double waterWeaknessDamageIncrease = 0.3;

	public static void weaken(Entity entity) {
		waterWeakness.put(entity.getEntityId(), System.currentTimeMillis());
	}

	boolean isWeakened(Entity entity) {
		return waterWeakness.containsKey(entity.getEntityId()) && waterWeakness.get(entity.getEntityId()) + waterWeaknessDuration > System.currentTimeMillis();
	}

	void flush() {
		Iterator<Entry<Integer, Long>> iterator = waterWeakness.entrySet().iterator();
		while (iterator.hasNext())
			if (iterator.next().getValue() + waterWeaknessDuration < System.currentTimeMillis())
				iterator.remove();
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void a(EntityDamageByEntityEvent event) {
		Entity entity = event.getEntity();
		if (isWeakened(entity)) {
			event.setDamage(event.getDamage() * (1 + waterWeaknessDamageIncrease));
			entity.getWorld().spawnParticle(Particle.WATER_SPLASH, event.getEntity().getLocation().add(0, entity.getHeight() / 2, 0), 16, .3, .3, .3, 0);
		}
	}
}
