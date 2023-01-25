package net.Indyuce.mmoitems.util;

@FunctionalInterface
public interface Provider<T> {
    T get();
}
