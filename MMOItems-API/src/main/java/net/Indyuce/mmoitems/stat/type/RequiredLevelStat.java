package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.RequiredLevelData;
import net.Indyuce.mmoitems.stat.data.random.RandomRequiredLevelData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Mostly used by external plugins to implement custom level
 * requirements like Heroes secondary hero level reqs or
 * AuraSkills/MMOCore for profession levels.
 *
 * @author Jules
 */
public abstract class RequiredLevelStat extends DoubleStat implements ItemRestriction, GemStoneStat {
    // private final String idKey;

    @Deprecated
    @BackwardsCompatibility(version = "6.10")
    public RequiredLevelStat(boolean ignored, String idKey, Material mat, String nameKey, String[] lore) {
        super(idKey, mat, "Required " + nameKey, lore, new String[]{"!block", "all"});

        // this.idKey = idKey;
    }

    public RequiredLevelStat(String idKey, Material mat, String nameKey, String[] lore) {
        super("REQUIRED_" + idKey, mat, "Required " + nameKey, lore, new String[]{"!block", "all"});

        // this.idKey = idKey;
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {

        // Lore Management
        final int requirement = (int) data.getValue();
        final String format = getGeneralStatFormat().replace("{value}", String.valueOf(requirement));
        item.getLore().insert(getPath(), format);

        // Insert NBT
        item.addItemTag(new ItemTag(getNBTPath(), requirement));
    }

    @Override
    public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull DoubleData currentData, @NotNull NumericStatFormula templateData) throws IllegalArgumentException {

        // Get Value
        double techMinimum = templateData.calculate(0, NumericStatFormula.FormulaInputType.LOWER_BOUND);
        double techMaximum = templateData.calculate(0, NumericStatFormula.FormulaInputType.UPPER_BOUND);

        // Cancel if it its NEGATIVE and this doesn't support negative stats.
        if (techMaximum < 0 && !handleNegativeStats()) return;

        if (techMinimum < 0 && !handleNegativeStats()) techMinimum = 0;
        if (techMinimum < templateData.getBase() - templateData.getMaxSpread())
            techMinimum = templateData.getBase() - templateData.getMaxSpread();
        if (techMaximum > templateData.getBase() + templateData.getMaxSpread())
            techMaximum = templateData.getBase() + templateData.getMaxSpread();

        // Add NBT Path
        item.addItemTag(getAppliedNBT(currentData));

        // Display if not ZERO
        if (techMinimum != 0 || techMaximum != 0) {
            final String builtRange = DoubleStat.formatPath(getPath(), getGeneralStatFormat(), false, false, Math.floor(techMinimum), Math.floor(techMaximum));
            item.getLore().insert(getPath(), builtRange);
        }
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull DoubleData data) {
        ArrayList<ItemTag> ret = new ArrayList<>();
        ret.add(new ItemTag(getNBTPath(), ((DoubleData) data).getValue()));
        return ret;
    }

    @Override
    public RandomRequiredLevelData whenInitialized(Object object) {
        return new RandomRequiredLevelData(object);
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Find relevant tgs
        ArrayList<ItemTag> tags = new ArrayList<>();
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            tags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.DOUBLE));

        // Build
        StatData data = getLoadedNBT(tags);

        // Valid?
        if (data != null)
            mmoitem.setData(this, data);
    }

    @Nullable
    @Override
    public RequiredLevelData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        ItemTag rTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);
        if (rTag != null)
            return new RequiredLevelData((Double) rTag.getValue());

        return null;
    }

    @Override
    public RequiredLevelData getClearStatData() {
        return new RequiredLevelData(0D);
    }
}
