package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkullTextureData implements StatData, RandomStatData<SkullTextureData> {

    /**
     * Spigot 1.20.2 introduced a PlayerProfile API which requires
     * to both support PlayerProfile and GameProfile objects as
     * reflection is no longer supported by >1.20.2
     */
    private Object profile;

    public SkullTextureData(Object profile) {
        this.profile = profile;
    }

    @Nullable
    public Object getGameProfile() {
        return profile;
    }

    public void setGameProfile(@Nullable Object profile) {
        this.profile = profile;
    }

    @NotNull
    @Override
    public SkullTextureData clone() {
        return new SkullTextureData(profile);
    }

    @Override
    public boolean isEmpty() {
        return profile == null;
    }

    @Override
    public SkullTextureData randomize(MMOItemBuilder builder) {
        return this;
    }
}