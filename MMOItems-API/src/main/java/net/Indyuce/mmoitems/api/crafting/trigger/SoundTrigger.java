package net.Indyuce.mmoitems.api.crafting.trigger;

import net.Indyuce.mmoitems.api.player.PlayerData;
import io.lumine.mythic.lib.api.MMOLineConfig;
import org.bukkit.Sound;

public class SoundTrigger extends Trigger {
	private final Sound sound;
	private final float vol, pitch;

	public SoundTrigger(MMOLineConfig config) {
		super("sound");

		config.validate("sound");

		sound = Sound.valueOf(config.getString("sound").toUpperCase().replace("-", "_"));
		vol = (float) config.getDouble("volume", 1);
		pitch = (float) config.getDouble("pitch", 1);
	}

	@Override
	public void whenCrafting(PlayerData data) {
		if(!data.isOnline()) return;
		data.getPlayer().playSound(data.getPlayer().getLocation(), sound, vol, pitch);
	}
}
