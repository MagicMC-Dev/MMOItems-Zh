package net.Indyuce.mmoitems.api.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundReader {
	private final Sound sound;

	public SoundReader(String tag, Sound defaultSound) {
		Sound sound;
		try {
			sound = Sound.valueOf(tag);
		} catch (Exception e) {
			sound = defaultSound;
		}
		this.sound = sound;
	}

	public Sound getSound() {
		return sound;
	}

	public void play(Player player) {
		play(player, 1, 1);
	}

	public void play(Player player, float vol, float pitch) {
		player.playSound(player.getLocation(), sound, vol, pitch);
	}
}
