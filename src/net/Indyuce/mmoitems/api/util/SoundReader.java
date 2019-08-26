package net.Indyuce.mmoitems.api.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SoundReader {
	private Sound sound;
	private boolean valid;

	public SoundReader(String tag, Sound defaultSound) {
		if (tag.equals("")) {
			sound = defaultSound;
			return;
		}

		try {
			sound = Sound.valueOf(tag);
			valid = true;
		} catch (Exception e) {
			sound = defaultSound;
		}
	}

	public Sound getSound() {
		return sound;
	}

	public void play(Player player) {
		play(player, 1, 1);
	}

	public void play(Player player, float vol, float pitch) {
		if (valid)
			player.playSound(player.getLocation(), sound, vol, pitch);
	}
}
