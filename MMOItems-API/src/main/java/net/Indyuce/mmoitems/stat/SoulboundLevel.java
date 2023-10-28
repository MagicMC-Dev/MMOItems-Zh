package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.jetbrains.annotations.NotNull;

/**
 * Soulbound level for consumables.
 */
public class SoulboundLevel extends DoubleStat {
    public SoulboundLevel() {
        super("SOULBOUND_LEVEL", VersionMaterial.ENDER_EYE.toMaterial(), "灵魂绑定等级", new String[]{"灵魂绑定等级决定了玩家在尝试使", "用灵魂绑定物品时会受到多少伤害", "\n它还决定了打破绑定的难度."}, new String[]{"consumable"});
    }

    // Writes soulbound level with roman writing in lore
    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {
        int value = (int) data.getValue();
        item.addItemTag(new ItemTag("MMOITEMS_SOULBOUND_LEVEL", value));
        item.getLore().insert("soulbound-level", getGeneralStatFormat().replace("{value}", MMOUtils.intToRoman(value)));
    }

    @Override
    public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull DoubleData currentData, @NotNull NumericStatFormula templateData) throws IllegalArgumentException {
        whenApplied(item, currentData);
    }
}
