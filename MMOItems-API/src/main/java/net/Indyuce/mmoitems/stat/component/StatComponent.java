package net.Indyuce.mmoitems.stat.component;

public abstract class StatComponent {
    private final String path;

    public StatComponent(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
