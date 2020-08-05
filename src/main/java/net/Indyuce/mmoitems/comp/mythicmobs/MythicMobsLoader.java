package net.Indyuce.mmoitems.comp.mythicmobs;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.comp.mythicmobs.stat.FactionDamage;

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
        for (MythicMob mob : MythicMobs.inst().getMobManager().getMobTypes()) {
            if (mob.getFaction() != null) {
                if (!allFactions.contains(mob.getFaction()))
                    allFactions.add(mob.getFaction());
            }
        }
        return allFactions;
    }
}
