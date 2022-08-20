package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.RequiredLevelData;
import net.Indyuce.mmoitems.stat.data.random.RandomRequiredLevelData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Used by {@link net.Indyuce.mmoitems.comp.rpg.AureliumSkillsHook} to handle
 * required skill levels.
 */
public abstract class RequiredLevelStat extends DoubleStat implements ItemRestriction, GemStoneStat {
    private final String idKey;

    public RequiredLevelStat(String idKey, Material mat, String nameKey, String[] lore) {
        super("REQUIRED_" + idKey,
                mat,
                "Required " + nameKey,
                lore,
                new String[]{"!block", "all"});

        this.idKey = idKey;
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {

        // Lore Management
        int lvl = (int) ((DoubleData) data).getValue();
        String format = MMOItems.plugin.getLanguage().getStatFormat(getPath()).replace("{value}", String.valueOf(lvl));
        item.getLore().insert(getPath(), format);

        // Insert NBT
        item.addItemTag(new ItemTag(getNBTPath(), lvl));
    }

    @Override
    public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull DoubleData currentData, @NotNull NumericStatFormula templateData) throws IllegalArgumentException {

        // Get Value
        double techMinimum = ((NumericStatFormula) templateData).calculate(0, -2.5);
        double techMaximum = ((NumericStatFormula) templateData).calculate(0, 2.5);

        // Cancel if it its NEGATIVE and this doesn't support negative stats.
        if (techMaximum < 0 && !handleNegativeStats()) {
            return;
        }
        if (techMinimum < 0 && !handleNegativeStats()) {
            techMinimum = 0;
        }
        if (techMinimum < ((NumericStatFormula) templateData).getBase() - ((NumericStatFormula) templateData).getMaxSpread()) {
            techMinimum = ((NumericStatFormula) templateData).getBase() - ((NumericStatFormula) templateData).getMaxSpread();
        }
        if (techMaximum > ((NumericStatFormula) templateData).getBase() + ((NumericStatFormula) templateData).getMaxSpread()) {
            techMaximum = ((NumericStatFormula) templateData).getBase() + ((NumericStatFormula) templateData).getMaxSpread();
        }

        // Add NBT Path
        item.addItemTag(getAppliedNBT(currentData));

        // Display if not ZERO
        if (techMinimum != 0 || techMaximum != 0) {

            String builtRange;
            if (SilentNumbers.round(techMinimum, 2) == SilentNumbers.round(techMaximum, 2)) {
                builtRange = SilentNumbers.readableRounding(techMinimum, 0);
            } else {
                builtRange = SilentNumbers.removeDecimalZeros(String.valueOf(techMinimum)) + "-" + SilentNumbers.removeDecimalZeros(String.valueOf(techMaximum));
            }

            // Just display normally
            item.getLore().insert(getPath(), formatNumericStat(techMinimum, "{value}", builtRange));
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

        // Find relevat tgs
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
