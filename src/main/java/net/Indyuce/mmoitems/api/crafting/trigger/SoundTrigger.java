package net.Indyuce.mmoitems.api.crafting.trigger;

import net.Indyuce.mmoitems.api.player.PlayerData;
import net.mmogroup.mmolib.api.MMOLineConfig;
import org.bukkit.Sound;

public class SoundTrigger extends Trigger {
    private final Sound sound;
    private final float vol, pitch;

    public SoundTrigger(MMOLineConfig config) {
        super("sound");

        config.validate("sound");

        sound = Sound.valueOf(config.getString("sound").toUpperCase().replace("-", "_"));
        vol = config.contains("volume") ? (float) config.getDouble("volume") : 1f;
        pitch = config.contains("pitch") ? (float) config.getDouble("pitch") : 1f;
    }

    @Override
    public void whenCrafting(PlayerData player) {
        player.getPlayer().playSound(player.getPlayer().getLocation(), sound, vol, pitch);
    }
}
