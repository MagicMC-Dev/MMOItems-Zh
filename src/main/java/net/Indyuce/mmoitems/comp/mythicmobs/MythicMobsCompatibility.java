package net.Indyuce.mmoitems.comp.mythicmobs;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicReloadedEvent;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.comp.mythicmobs.skill.MythicMobsAbility;
import net.Indyuce.mmoitems.comp.mythicmobs.stat.FactionDamage;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.logging.Level;

public class MythicMobsCompatibility implements Listener {

    public MythicMobsCompatibility() {

        // Gonna keep the try catch here for a safety net.
        try {
            for (String faction : this.getFactions())
                MMOItems.plugin.getStats().register(new FactionDamage(faction));
        } catch (NullPointerException ignored) {
        }

        Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);
    }

    @EventHandler
    public void a(MythicReloadedEvent event) {

        // Update skills
        MMOItems.plugin.getAbilities().getAll().stream().filter(ability -> ability instanceof MythicMobsAbility).map(ability -> (MythicMobsAbility) ability).forEach(ability -> {
            try {
                Optional<io.lumine.xikage.mythicmobs.skills.Skill> opt = MythicMobs.inst().getSkillManager().getSkill(ability.getInternalName());
                Validate.isTrue(opt.isPresent(), "Could not find MM skill with name '" + ability.getInternalName() + "'");
                ability.setSkill(opt.get());
            } catch (IllegalArgumentException exception) {
                MMOItems.plugin.getLogger().log(Level.WARNING, "Could not reload custom MM ability '" + ability.getID() + "': " + exception.getMessage());
            }
        });
    }

    private Set<String> getFactions() {
        Set<String> allFactions = new HashSet<>();

        // Collects all mythic mobs + edited vanilla mobs in mythic mobs.
        List<MythicMob> mobs = new ArrayList<>(MythicMobs.inst().getMobManager().getVanillaTypes());
        mobs.addAll(MythicMobs.inst().getMobManager().getMobTypes());
        // Adds their faction to the set if it is set.

        for (MythicMob mob : mobs)
            // Checks if it has a faction.
            if (mob.hasFaction())
                allFactions.add(mob.getFaction());

        return allFactions;
    }
}
