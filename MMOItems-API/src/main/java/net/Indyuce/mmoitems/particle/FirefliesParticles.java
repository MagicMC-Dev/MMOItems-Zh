package net.Indyuce.mmoitems.particle;

import org.bukkit.Location;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.particle.api.ParticleRunnable;
import net.Indyuce.mmoitems.stat.data.ParticleData;

public class FirefliesParticles extends ParticleRunnable {
	private final float speed, height, radius, r_speed;
	private final int amount;

	private double j = 0;

	public FirefliesParticles(ParticleData particle, PlayerData player) {
		super(particle, player);

		speed = (float) particle.getModifier("speed");
		height = (float) particle.getModifier("height");
		radius = (float) particle.getModifier("radius");
		r_speed = (float) particle.getModifier("rotation-speed");
		amount = (int) particle.getModifier("amount");
	}

	@Override
	public void createParticles() {
		Location loc = player.getPlayer().getLocation();
		for (int k = 0; k < amount; k++) {
			double a = j + Math.PI * 2 * k / amount;
			particle.display(loc.clone().add(Math.cos(a) * radius, height, Math.sin(a) * radius), speed);
		}

		j += Math.PI / 48 * r_speed;
		j -= j > Math.PI * 2 ? Math.PI * 2 : 0;
	}
}
