package net.Indyuce.mmoitems.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Pair<K, V> {
    private final K key;
    private final V value;

    public Pair(@NotNull K var1, @NotNull V var2) {
        this.key = var1;
        this.value = var2;
    }

    public static <K, V> Pair<K, V> of(@NotNull K var0, @NotNull V var1) {
        return new Pair(var0, var1);
    }

    public K getKey() {
        return this.key;
    }

    public V getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return key.equals(pair.key) && value.equals(pair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
}