package net.Indyuce.mmoitems.api.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundReader {
	private final Sound sound;
	private final String soundKey;

	public SoundReader(String tag, Sound defaultSound) {
		if(tag.isEmpty()) {
			this.sound = defaultSound;
			this.soundKey = "";
			return;
		}

		Sound sound;
		String soundKey;
		try {
			sound = Sound.valueOf(tag);
			soundKey = "";
		} catch (Exception e) {
			sound = null;
			soundKey = tag;
		}

		this.sound = sound;
		this.soundKey = soundKey.toLowerCase();
	}

	public Sound getSound() {
		return sound;
	}

	public String getSoundKey() {
		return soundKey;
	}

	public void play(Player player) {
		play(player, 1, 1);
	}

	public void play(Player player, float vol, float pitch) {
		if(soundKey.isEmpty())
			player.playSound(player.getLocation(), sound, vol, pitch);
		else
			player.playSound(player.getLocation(), soundKey, vol, pitch);
	}

	public void play(Location loc) {
		play(loc, 1, 1);
	}

	public void play(Location loc, float vol, float pitch) {
		if(soundKey.isEmpty())
			loc.getWorld().playSound(loc, sound, vol, pitch);
		else
			loc.getWorld().playSound(loc, soundKey, vol, pitch);
	}
}
