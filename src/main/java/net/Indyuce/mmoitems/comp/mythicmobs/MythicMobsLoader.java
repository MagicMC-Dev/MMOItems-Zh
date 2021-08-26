package net.Indyuce.mmoitems.comp.mythicmobs;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.comp.mythicmobs.stat.FactionDamage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MythicMobsLoader {

    public MythicMobsLoader() {
        // Gonna keep the try catch here for a safety net.
        try {
            for (String faction : this.getFactions()) {
                MMOItems.plugin.getStats().register(new FactionDamage(faction));
            }
        } catch (NullPointerException ignored) {}
    }

    private Set<String> getFactions(){
        // Returned Set.
        Set<String> allFactions = new HashSet<>();
        // Collects all mythic mobs + edited vanilla mobs in mythic mobs.
        List<MythicMob> mobs = new ArrayList<>(MythicMobs.inst().getMobManager().getVanillaTypes());
        mobs.addAll(MythicMobs.inst().getMobManager().getMobTypes());
        // Adds their faction to the set if it is set.
        for (MythicMob mob : mobs) {
            // Checks if it has a faction.
            if (mob.hasFaction())
                allFactions.add(mob.getFaction());
        }
        return allFactions;
    }
}
