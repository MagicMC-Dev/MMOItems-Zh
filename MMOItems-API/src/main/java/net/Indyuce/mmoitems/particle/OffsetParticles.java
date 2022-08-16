package net.Indyuce.mmoitems.particle;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.particle.api.ParticleRunnable;
import net.Indyuce.mmoitems.stat.data.ParticleData;

public class OffsetParticles extends ParticleRunnable {
	private final float speed, h_offset, v_offset, height;
	private final int amount;

	public OffsetParticles(ParticleData particle, PlayerData player) {
		super(particle, player);

		speed = (float) particle.getModifier("speed");
		height = (float) particle.getModifier("height");
		h_offset = (float) particle.getModifier("horizontal-offset");
		v_offset = (float) particle.getModifier("vertical-offset");
		amount = (int) particle.getModifier("amount");
	}

	@Override
	public void createParticles() {
		particle.display(player.getPlayer().getLocation().add(0, height, 0), amount, h_offset, v_offset, h_offset, speed);
	}
}
