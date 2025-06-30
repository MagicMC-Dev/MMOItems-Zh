package net.Indyuce.mmoitems.comp.mythicmobs;

import io.lumine.mythic.bukkit.MythicBukkit;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.comp.mythicmobs.stat.FactionDamage;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MythicMobsLoadHook implements Listener {

    /**
     * When the plugin loads
     */
    public MythicMobsLoadHook() {

        // Collect factions and create item stats
        registerFactionStats(true);
    }

    public static void registerFactionStats(boolean fromConfig) {

        // [BUGFIX] right now, mythicmobs does not allow to get factions when mythicmobs
        // just loaded because all mobs are checked when enabling the plugin. need for
        // users to temporarily
        // TODO improve on this later with MI7 with custom damage types
        for (String faction : fromConfig ? _collectFactions() : collectFactions()) MMOItems.plugin.getStats().register(new FactionDamage(faction));
    }

    private static Set<String> collectFactions() {

        // Using a set to kill duplicates
        var allFactions = new HashSet<String>();

        // Collects all mythic mobs + edited vanilla mobs in mythic mobs.
        for (var mob : MythicBukkit.inst().getMobManager().getVanillaTypes())
            if (mob.hasFaction()) allFactions.add(mob.getFaction().toUpperCase());
        for (var mob : MythicBukkit.inst().getMobManager().getMobTypes())
            if (mob.hasFaction()) allFactions.add(mob.getFaction().toUpperCase());

        return allFactions;
    }

    private static List<String> _collectFactions() {
        var tempConfigFile = new ConfigFile(MMOItems.plugin, "/plugindata", "mm_factions").getConfig();
        return tempConfigFile.getStringList(FACTION_DATA_KEY);
    }

    private static final String FACTION_DATA_KEY = "data";

    public static void saveTrueMobFactions() {
        var tempConfigFile = new ConfigFile(MMOItems.plugin, "/plugindata", "mm_factions");
        tempConfigFile.getConfig().set("data", new ArrayList<>(collectFactions()));
        tempConfigFile.save();
    }
}
