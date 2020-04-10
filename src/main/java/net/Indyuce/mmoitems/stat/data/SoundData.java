package net.Indyuce.mmoitems.stat.data;

public class SoundData {
	private final String sound;
	private final double volume, pitch;

	public SoundData(String s, double v, double p) {
		this.sound = s;
		this.volume = v;
		this.pitch = p;
	}

	public String getSound() {
		return this.sound;
	}

	public double getVolume() {
		return this.volume;
	}

	public double getPitch() {
		return this.pitch;
	}
}