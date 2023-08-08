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
            NEVER = new StatChoice("NEVER", "宝石属性不会因物品升级而增加或减少"),
            HISTORIC = new StatChoice("HISTORIC", "宝石属性会立即升级到当前的物品属性,之后升级也是如此"),
            SUBSEQUENT = new StatChoice("SUBSEQUENT", "宝石属性通过升级物品进行增加或减少,但只有在将宝石镶嵌后");

    /**
     * Can't be final as it is a plugin configuration option
     */
    public static String defaultValue = SUBSEQUENT.getId();

    public GemUpgradeScaling() {
        super("GEM_UPGRADE_SCALING", VersionMaterial.LIME_DYE.toMaterial(), "宝石升级缩放", new String[] { "宝石将属性添加到物品中", "但您也可以通过制作站或消耗品升级您的物品.", "", "§6升级时,此宝石属性会受到影响吗？" }, new String[] { "gem_stone" });

        // Set the acceptable values
        addChoices(SUBSEQUENT, NEVER, HISTORIC);
    }

    @NotNull
    @Override
    public StringData getClearStatData() { return new StringData(defaultValue); }
}
