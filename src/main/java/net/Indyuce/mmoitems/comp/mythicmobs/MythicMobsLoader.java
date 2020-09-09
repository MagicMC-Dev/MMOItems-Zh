package net.Indyuce.mmoitems.comp.mythicmobs;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.comp.mythicmobs.stat.FactionDamage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/*
 * this loads mmoitems comp for mythic mobs
 * since MythicMobsHook is purely for documentation
 */
public class MythicMobsLoader {

    public MythicMobsLoader() {
        for (String faction : this.getFactions()) {
            MMOItems.plugin.getStats().register(new FactionDamage(faction));
        }
    }

    private Collection<String> getFactions(){
        Collection<String> allFactions = new LinkedList<>();
        Collection<MythicMob> mobs = new ArrayList<>(MythicMobs.inst().getMobManager().getVanillaTypes());
        mobs.addAll(MythicMobs.inst().getMobManager().getMobTypes());
        for (MythicMob mob : mobs) {
            if (mob.getFaction() != null && !allFactions.contains(mob.getFaction())) {
                    allFactions.add(mob.getFaction());
            }
        }
        return allFactions;
    }
}
