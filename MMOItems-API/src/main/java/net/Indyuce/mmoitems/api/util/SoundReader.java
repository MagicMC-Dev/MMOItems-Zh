package net.Indyuce.mmoitems.api.util;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoundReader {
    @Nullable
    private final Sound sound;
    @Nullable
    private final String soundKey;

    public SoundReader(@NotNull String tag, @NotNull Sound defaultSound) {
        if (tag.isEmpty()) {
            this.sound = defaultSound;
            this.soundKey = "";
            return;
        }

        Sound sound;
        String soundKey;
        try {
            sound = Sound.valueOf(tag);
            soundKey = null;
        } catch (Exception exception) {
            sound = null;
            soundKey = tag.toLowerCase();
        }

        this.sound = sound;
        this.soundKey = soundKey;
    }

    @Nullable
    public Sound getSound() {
        return sound;
    }

    @NotNull
    public String getSoundKey() {
        return soundKey;
    }

    public void play(@NotNull Player player) {
        play(player, 1, 1);
    }

    public void play(@NotNull Player player, float vol, float pitch) {
        if (sound != null) player.playSound(player.getLocation(), sound, vol, pitch);
        else player.playSound(player.getLocation(), soundKey, vol, pitch);
    }

    public void play(@NotNull Location loc) {
        play(loc, 1, 1);
    }

    public void play(@NotNull Location loc, float vol, float pitch) {
        if (sound != null) loc.getWorld().playSound(loc, sound, vol, pitch);
        else loc.getWorld().playSound(loc, soundKey, vol, pitch);
    }
}
