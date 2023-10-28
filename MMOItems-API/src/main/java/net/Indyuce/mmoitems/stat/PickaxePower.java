package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class PickaxePower extends DoubleStat {
    public PickaxePower() {
        super("PICKAXE_POWER", Material.IRON_PICKAXE, "挖掘等级", new String[]{"挖掘自定义方块时的挖掘速度"}, new String[]{"tool"});
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {
        int pickPower = (int) data.getValue();

        item.addItemTag(new ItemTag("MMOITEMS_PICKAXE_POWER", pickPower));
        item.getLore().insert("pickaxe-power", DoubleStat.formatPath(getPath(), getGeneralStatFormat(), true, false, pickPower));
    }

    @Override
    public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull DoubleData currentData, @NotNull NumericStatFormula templateData) throws IllegalArgumentException {
        Validate.isTrue(currentData instanceof DoubleData, "当前数据不是双精度数据");
        Validate.isTrue(templateData instanceof NumericStatFormula, "模板数据不是数字统计公式");

        // Get Value
        double techMinimum = templateData.calculate(0, NumericStatFormula.FormulaInputType.LOWER_BOUND);
        double techMaximum = templateData.calculate(0, NumericStatFormula.FormulaInputType.UPPER_BOUND);

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
        item.addItemTag(new ItemTag("MMOITEMS_PICKAXE_POWER", currentData.getValue()));

        // Display if not ZERO
        if (techMinimum != 0 || techMaximum != 0) {
            String builtRange = DoubleStat.formatPath(getPath(), getGeneralStatFormat(), true, false, Math.floor(techMinimum), Math.floor(techMaximum));
            item.getLore().insert("pickaxe-power", builtRange);
        }
    }
}
