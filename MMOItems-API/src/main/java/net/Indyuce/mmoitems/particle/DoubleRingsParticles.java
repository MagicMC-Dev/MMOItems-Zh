package net.Indyuce.mmoitems.particle;

import org.bukkit.Location;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.particle.api.ParticleRunnable;
import net.Indyuce.mmoitems.stat.data.ParticleData;

public class DoubleRingsParticles extends ParticleRunnable {
	private final float speed, height, radius, r_speed, y_offset;

	private double j = 0;

	public DoubleRingsParticles(ParticleData particle, PlayerData player) {
		super(particle, player);

		speed = (float) particle.getModifier("speed");
		height = (float) particle.getModifier("height");
		radius = (float) particle.getModifier("radius");
		r_speed = (float) particle.getModifier("rotation-speed");
		y_offset = (float) particle.getModifier("y-offset");
	}

	@Override
	public void createParticles() {
		Location loc = player.getPlayer().getLocation();
		for (double k = 0; k < 2; k++) {
			double a = j + k * Math.PI;
			particle.display(loc.clone().add(radius * Math.cos(a), height + Math.sin(j) * y_offset, radius * Math.sin(a)), speed);
		}

		j += Math.PI / 16 * r_speed;
		j -= j > Math.PI * 2 ? Math.PI * 2 : 0;
	}
}
