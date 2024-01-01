package net.Indyuce.mmoitems.stat.component.type;

import net.Indyuce.mmoitems.stat.component.StatComponent;
import net.Indyuce.mmoitems.stat.component.Upgradable;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @deprecated Not used yet
 */
@Deprecated
public class ObjectComponent extends AbstractObjectComponent implements Upgradable {
    private final Map<String, StatComponent> components = new HashMap<>();

    public ObjectComponent(String path) {
        super(path);
    }

    @Override
    @Nullable
    public StatComponent getComponent(String path) {
        return components.get(path);
    }

    public void forEachComponent(Consumer<StatComponent> action) {
        for (StatComponent component : components.values())
            action.accept(component);
    }

    public Set<String> getComponentKeys() {
        return components.keySet();
    }
}
