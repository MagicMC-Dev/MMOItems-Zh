package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ChooseStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Defines how gem stats will scale when the item they are put on upgrades.
 */
public class GemUpgradeScaling extends ChooseStat implements GemStoneStat {
    public static final String NEVER = "NEVER", HISTORIC = "HISTORIC", SUBSEQUENT = "SUBSEQUENT";
    public static String defaultValue = SUBSEQUENT;

    public GemUpgradeScaling() {
        super("GEM_UPGRADE_SCALING", VersionMaterial.LIME_DYE.toMaterial(), "Gem Upgrade Scaling", new String[] { "Gem stones add their stats to items, but you may also", "upgrade your items via crafting stations or consumables.", "", "\u00a76Should this gem stone stats be affected by upgrading?" }, new String[] { "gem_stone" });

        // Create the list
        ArrayList<String> applicationScalingTypes = new ArrayList<>();
        Collections.addAll(applicationScalingTypes, SUBSEQUENT, NEVER, HISTORIC);

        // Set the acceptable values
        InitializeChooseableList(applicationScalingTypes);

        // Put definitions
        HashMap<String, String> definitions = new HashMap<>();
        definitions.put(SUBSEQUENT, "Gem stats scale by upgrading the item, but only after putting the gem in.");
        definitions.put(NEVER, "Gem stats are never scaled by upgrading the item.");
        definitions.put(HISTORIC, "Gem stats instantly upgrade to the current item level, and subsequently thereafter.");

        // Update
        HintChooseableDefs(definitions);
    }

    @NotNull @Override public StatData getClearStatData() { return new StringData(defaultValue); }
}
