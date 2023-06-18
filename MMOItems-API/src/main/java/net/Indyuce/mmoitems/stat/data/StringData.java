package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.Mergeable;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringData implements StatData, RandomStatData<StringData>, Mergeable<StringData> {
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

    @Override
    public void merge(@Nullable StringData data) {

        // Overwrite
        value = data.getString();
    }

    @NotNull
    @Override
    public StringData cloneData() {
        return new StringData(value);
    }
}