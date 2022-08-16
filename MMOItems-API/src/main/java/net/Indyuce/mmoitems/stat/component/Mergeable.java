package net.Indyuce.mmoitems.stat.component;

public interface Mergeable<T extends StatComponent> {

    public void merge(T t);
}
