package net.Indyuce.mmoitems.api.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;

import net.Indyuce.mmoitems.MMOItems;

public class MMORayTraceResult {
	private final LivingEntity entity;
	private final double range;

	public MMORayTraceResult(LivingEntity entity, double range) {
		this.range = range;
		this.entity = entity;
	}

	public boolean hasHit() {
		return entity != null;
	}

	public LivingEntity getHit() {
		return entity;
	}

	public double getRange() {
		return range;
	}

	public void draw(Location source, Vector vec, double c, Color color) {
		draw(source, vec, c, (loc) -> MMOItems.plugin.getVersion().getWrapper().spawnParticle(Particle.REDSTONE, loc, color));
	}

	public void draw(Location loc, Vector vec, double c, Consumer<Location> tick) {
		vec = vec.multiply(1 / c);
		for (int j = 0; j < range * c; j++)
			tick.accept(loc.add(vec));
	}
}
