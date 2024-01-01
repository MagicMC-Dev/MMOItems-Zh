package net.Indyuce.mmoitems.stat.component.type;

import net.Indyuce.mmoitems.stat.component.StatComponent;
import org.jetbrains.annotations.Nullable;

/**
 * @deprecated Not used yet
 */
@Deprecated
public abstract class AbstractObjectComponent extends StatComponent {
    public AbstractObjectComponent(String path) {
        super(path);
    }

    @Nullable
    public StatComponent findComponent(String path) {
        String[] split = path.split("\\.");

        AbstractObjectComponent current = this;
        int n = split.length - 1;
        for (int i = 0; i < n; i++) {
            StatComponent next = getComponent(split[i]);
            if (next == null || !(next instanceof AbstractObjectComponent))
                return null;

            current = (AbstractObjectComponent) next;
        }

        return current.getComponent(split[n]);
    }

    @Nullable
    public abstract StatComponent getComponent(String path);


}
