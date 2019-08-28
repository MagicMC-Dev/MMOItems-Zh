package net.Indyuce.mmoitems.particle;

import org.bukkit.Location;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.particle.api.ParticleRunnable;
import net.Indyuce.mmoitems.stat.data.ParticleData;

public class HelixParticles extends ParticleRunnable {
	private float speed, height, radius, r_speed, y_speed;
	private int amount;

	private double j = 0;

	public HelixParticles(ParticleData particle, PlayerData player) {
		super(particle, player);

		speed = (float) particle.getModifier("speed");
		height = (float) particle.getModifier("height");
		radius = (float) particle.getModifier("radius");
		r_speed = (float) particle.getModifier("rotation-speed");
		y_speed = (float) particle.getModifier("y-speed");
		amount = (int) particle.getModifier("amount");
	}

	@Override
	public void run() {
		Location loc = player.getPlayer().getLocation();
		for (double k = 0; k < amount; k++) {
			double a = j + k * Math.PI * 2 / amount;
			particle.display(loc.clone().add(Math.cos(a) * Math.cos(j * y_speed) * radius, 1 + Math.sin(j * y_speed) * height, Math.sin(a) * Math.cos(j * y_speed) * radius), speed);
		}

		j += Math.PI / 24 * r_speed;
		j -= j > Math.PI * 2 / y_speed ? Math.PI * 2 / y_speed : 0;
	}
}
