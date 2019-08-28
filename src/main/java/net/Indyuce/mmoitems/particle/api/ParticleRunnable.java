package net.Indyuce.mmoitems.particle.api;

import org.bukkit.scheduler.BukkitRunnable;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.stat.data.ParticleData;

public abstract class ParticleRunnable extends BukkitRunnable {
	protected final ParticleData particle;
	protected final PlayerData player;

	public ParticleRunnable(ParticleData particle, PlayerData player) {
		this.particle = particle;
		this.player = player;
	}

	@Override
	public abstract void run();
}
