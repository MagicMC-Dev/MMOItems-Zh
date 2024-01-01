package net.Indyuce.mmoitems.stat.component;

/**
 * @deprecated Not used yet
 */
@Deprecated
public abstract class StatComponent {
    private final String path;

    public StatComponent(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
