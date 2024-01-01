package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringData implements StatData, RandomStatData<StringData> {
    @Nullable
    private String value;

    public StringData(@Nullable String str) {
        this.value = str;
    }

    @Nullable
    public String getString() {
        return value;
    }

    public void setString(@Nullable String str) {
        this.value = str;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public StringData randomize(MMOItemBuilder builder) {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }

    @NotNull
    @Override
    public StringData clone() {
        return new StringData(value);
    }
}