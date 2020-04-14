package net.Indyuce.mmoitems.api.player;

public class CooldownInformation {
	private final long castTime = System.currentTimeMillis(), cooldown;
	private long available;

	public CooldownInformation(double cooldown) {
		this.cooldown = (long) (cooldown * 1000);
		this.available = (long) (System.currentTimeMillis() + cooldown * 1000);
	}

	public double getInitialCooldown() {
		return (double) cooldown / 1000d;
	}

	public boolean hasCooledDown() {
		return System.currentTimeMillis() > available;
	}

	public double getRemaining() {
		return Math.max(0, available - System.currentTimeMillis()) / 1000.;
	}

	public long getCastTime() {
		return castTime;
	}

	public void reduceCooldown(double value) {
		available -= value * 100;
	}
}
