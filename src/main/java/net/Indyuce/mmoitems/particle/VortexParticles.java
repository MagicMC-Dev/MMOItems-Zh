package net.Indyuce.mmoitems.particle;

import org.bukkit.Location;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.particle.api.ParticleRunnable;
import net.Indyuce.mmoitems.stat.data.ParticleData;

public class VortexParticles extends ParticleRunnable {
	private final float speed, height, radius, r_speed, y_speed;
	private final int amount;

	private double j = 0;

	public VortexParticles(ParticleData particle, PlayerData player) {
		super(particle, player);

		speed = (float) particle.getModifier("speed");
		height = (float) particle.getModifier("height");
		radius = (float) particle.getModifier("radius");
		y_speed = (float) particle.getModifier("y-speed");
		r_speed = (float) particle.getModifier("rotation-speed");
		amount = (int) particle.getModifier("amount");
	}

	@Override
	public void createParticles() {
		Location loc = player.getPlayer().getLocation();
		double r = j / Math.PI / 2;
		for (int k = 0; k < amount; k++) {
			double a = j + Math.PI * 2 * k / amount;
			particle.display(loc.clone().add(Math.cos(a) * radius * (1 - r * y_speed), r * y_speed * height, Math.sin(a) * radius * (1 - r * y_speed)), speed);
		}

		j += Math.PI / 24 * r_speed;
		j -= j > Math.PI * 2 / y_speed ? Math.PI * 2 / y_speed : 0;
	}
}
