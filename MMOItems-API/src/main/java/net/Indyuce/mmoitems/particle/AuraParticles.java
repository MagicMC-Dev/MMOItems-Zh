package net.Indyuce.mmoitems.particle;

import org.bukkit.Location;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.particle.api.ParticleRunnable;
import net.Indyuce.mmoitems.stat.data.ParticleData;

public class AuraParticles extends ParticleRunnable {
	private final float speed, height, radius, r_speed, y_offset, y_speed;
	private final int amount;

	private double j = 0;
	
	public AuraParticles(ParticleData particle, PlayerData player) {
		super(particle, player);

		speed = (float) particle.getModifier("speed");
		height = (float) particle.getModifier("height");
		radius = (float) particle.getModifier("radius");
		r_speed = (float) particle.getModifier("rotation-speed");
		y_speed = (float) particle.getModifier("y-speed");
		y_offset = (float) particle.getModifier("y-offset");
		amount = (int) particle.getModifier("amount");
	}

	@Override
	public void createParticles() {
		Location loc = player.getPlayer().getLocation();
		for (int k = 0; k < amount; k++) {
			double a = j + Math.PI * 2 * k / amount;
			particle.display(loc.clone().add(Math.cos(a) * radius, Math.sin(j * y_speed * 3) * y_offset + height, Math.sin(a) * radius), speed);
		}

		j += Math.PI / 48 * r_speed;
		j -= j > Math.PI * 2 / y_speed ? Math.PI * 2 / y_speed : 0;
	}
}
