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
            NEVER = new StatChoice("NEVER", "Gem stats are never scaled by upgrading the item."),
            HISTORIC = new StatChoice("HISTORIC", "Gem stats instantly upgrade to the current item level, and subsequently thereafter."),
            SUBSEQUENT = new StatChoice("SUBSEQUENT", "Gem stats scale by upgrading the item, but only after putting the gem in.");

    /**
     * Can't be final as it is a plugin configuration option
     */
    public static String defaultValue = SUBSEQUENT.getId();

    public GemUpgradeScaling() {
        super("GEM_UPGRADE_SCALING", VersionMaterial.LIME_DYE.toMaterial(), "Gem Upgrade Scaling", new String[] { "Gem stones add their stats to items, but you may also", "upgrade your items via crafting stations or consumables.", "", "\u00a76Should this gem stone stats be affected by upgrading?" }, new String[] { "gem_stone" });

        // Set the acceptable values
        addChoices(SUBSEQUENT, NEVER, HISTORIC);
    }

    @NotNull
    @Override
    public StringData getClearStatData() { return new StringData(defaultValue); }
}
