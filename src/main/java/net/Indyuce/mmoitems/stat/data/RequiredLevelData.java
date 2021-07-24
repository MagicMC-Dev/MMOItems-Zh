package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;

/**
 * When a gem stone is applied onto an item with a lower level
 * requirement, the new item must save the HIGHEST level requirement
 * of the two so that newbies cannot use over powered items.
 * <p>
 * Hence the need to create a RequiredLevelData for that custom merge function.
 * <p>
 * Used by the 'required level' item stat as well as the 'required profession'
 * stats for both MMOCore and AureliumSkills
 */
public class RequiredLevelData extends DoubleData {
    public RequiredLevelData(double value) {
        super(value);
    }

    @Override
    public void merge(StatData data) {
        Validate.isTrue(data instanceof RequiredLevelData, "Cannot merge two different stat data types");
        boolean additiveMerge = MMOItems.plugin.getConfig().getBoolean("stat-merging.additive-levels", false);

        // Adding up
        if (additiveMerge) {

            // Additive
            setValue(((RequiredLevelData) data).getValue() + getValue());

        } else {

            // Max Level
            setValue(Math.max(((RequiredLevelData) data).getValue(), getValue()));
        }
    }

    @Override
    public @NotNull
    StatData cloneData() {
        return new RequiredLevelData(getValue());
    }

    @Override
    public String toString() {
        return String.valueOf(getValue());
    }
}
