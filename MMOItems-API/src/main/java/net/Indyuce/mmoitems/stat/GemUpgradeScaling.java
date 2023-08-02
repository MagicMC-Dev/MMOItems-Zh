package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ChooseStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.util.StatChoice;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Defines how gem stats will scale when the item they are put on upgrades.
 */
public class GemUpgradeScaling extends ChooseStat implements GemStoneStat {
    public static final StatChoice
            NEVER = new StatChoice("NEVER", "宝石统计数据永远不会通过升级物品来缩放。"),
            HISTORIC = new StatChoice("HISTORIC", "宝石统计数据立即升级到当前物品等级，随后也是如此。"),
            SUBSEQUENT = new StatChoice("SUBSEQUENT", "宝石统计数据通过升级物品来缩放，但仅限于放入宝石后。");

    /**
     * Can't be final as it is a plugin configuration option
     */
    public static String defaultValue = SUBSEQUENT.getId();

    public GemUpgradeScaling() {
        super("GEM_UPGRADE_SCALING", VersionMaterial.LIME_DYE.toMaterial(), "宝石升级缩放", new String[] { "宝石将其统计数据添加到物品中，但您也", "可以通过制作站或消耗品升级您的物品。", "", "\u00a76该宝石统计数据是否会受到升级的影响?" }, new String[] { "gem_stone" });

        // Set the acceptable values
        addChoices(SUBSEQUENT, NEVER, HISTORIC);
    }

    @NotNull
    @Override
    public StringData getClearStatData() { return new StringData(defaultValue); }
}
