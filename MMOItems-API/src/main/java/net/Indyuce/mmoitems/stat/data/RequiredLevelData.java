package net.Indyuce.mmoitems.stat.data;

import net.Indyuce.mmoitems.MMOItems;

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
    public void mergeWith(DoubleData data) {
        final boolean additiveMerge = MMOItems.plugin.getConfig().getBoolean("stat-merging.additive-levels", false);
        setValue(additiveMerge ? data.getValue() + getValue() : Math.max(data.getValue(), getValue()));
    }

    @Override
    public DoubleData clone() {
        return new RequiredLevelData(getValue());
    }
}
