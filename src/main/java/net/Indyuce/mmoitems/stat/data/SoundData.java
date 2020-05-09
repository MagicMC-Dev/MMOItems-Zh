package net.Indyuce.mmoitems.stat.data;

public class SoundData {
	private final String sound;
	private final double volume, pitch;

	/*
	 * using string for sound names so it supports custom sounds from resource
	 * packs
	 */
	public SoundData(String sound, double volume, double pitch) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}

	public String getSound() {
		return sound;
	}

	public double getVolume() {
		return volume;
	}

	public double getPitch() {
		return pitch;
	}
}