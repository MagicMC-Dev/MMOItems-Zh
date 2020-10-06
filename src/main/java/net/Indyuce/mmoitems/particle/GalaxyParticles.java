package net.Indyuce.mmoitems.particle;

import org.bukkit.Location;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.particle.api.ParticleRunnable;
import net.Indyuce.mmoitems.stat.data.ParticleData;

public class GalaxyParticles extends ParticleRunnable {
	private final float speed, height, r_speed, y_coord;
	private final int amount;

	private double j = 0;

	public GalaxyParticles(ParticleData particle, PlayerData player) {
		super(particle, player);

		speed = (float) particle.getModifier("speed") * .2f;
		height = (float) particle.getModifier("height");
		r_speed = (float) particle.getModifier("rotation-speed");
		y_coord = (float) particle.getModifier("y-coord");
		amount = (int) particle.getModifier("amount");
	}

	@Override
	public void createParticles() {
		Location loc = player.getPlayer().getLocation();
		for (int k = 0; k < amount; k++) {
			double a = j + Math.PI * 2 * k / amount;
			particle.display(loc.clone().add(0, height, 0), 0, (float) Math.cos(a), y_coord, (float) Math.sin(a), speed);
		}

		j += Math.PI / 24 * r_speed;
		j -= j > Math.PI * 2 ? Math.PI * 2 : 0;
	}
}
