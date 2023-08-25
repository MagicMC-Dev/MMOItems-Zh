package net.Indyuce.mmoitems.stat.data;

import org.bukkit.configuration.ConfigurationSection;

public class SoundData {
	private final String sound;
	private final double volume, pitch;

	/**
	 * @param sound
	 *            Sound name, supports resource packs because it is not the
	 *            Sound spigot enum
	 * @param volume
	 *            Sound volume
	 * @param pitch
	 *            Sound pitch from 0.5 to 2
	 */
	public SoundData(String sound, double volume, double pitch) {
		this.sound = sound;
		this.volume = volume;
		this.pitch = pitch;
	}

	/**
	 * Loads a sound from a config file
	 */
	public SoundData(Object object) {

		if (object instanceof String) {
			sound = object.toString();
			volume = 1;
			pitch = 1;
		}

		else if (object instanceof ConfigurationSection) {
			ConfigurationSection config = (ConfigurationSection) object;
			sound = config.getString("sound");
			volume = config.getDouble("volume");
			pitch = config.getDouble("pitch");
		}

		else
			throw new IllegalArgumentException("You must provide a string or config section");
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