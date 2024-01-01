package net.Indyuce.mmoitems.stat.component.type;

import net.Indyuce.mmoitems.stat.component.Mergeable;
import net.Indyuce.mmoitems.stat.component.StatComponent;
/**
 * @deprecated Not used yet
 */
@Deprecated
public class DoubleComponent extends StatComponent implements Mergeable<DoubleComponent> {
    private double value;

    public DoubleComponent(String path) {
        super(path);
    }

    @Override
    public void merge(DoubleComponent component) {
        value += component.value;
    }
}
