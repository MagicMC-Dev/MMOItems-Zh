package net.Indyuce.mmoitems.comp.mythicmobs;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import io.lumine.mythic.bukkit.events.MythicReloadedEvent;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.ingredient.MythicIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.inventory.MythicPlayerIngredient;
import net.Indyuce.mmoitems.api.crafting.output.MythicRecipeOutput;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.comp.mythicmobs.crafting.MythicMobsSkillTrigger;
import net.Indyuce.mmoitems.comp.mythicmobs.mechanics.MMOItemsArrowVolleyMechanic;
import net.Indyuce.mmoitems.comp.mythicmobs.mechanics.MMOItemsOnUseAura;
import net.Indyuce.mmoitems.comp.mythicmobs.stat.FactionDamage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

import java.util.HashSet;
import java.util.Set;

public class MythicMobsCompatibility implements Listener {

    public MythicMobsCompatibility() {
        Bukkit.getPluginManager().registerEvents(this, MMOItems.plugin);

        // Crafting Stations stuff
        MMOItems.plugin.getCrafting().registerTrigger("mmskill", MythicMobsSkillTrigger::new);
        MMOItems.plugin.getCrafting().registerIngredient("mythic",
                MythicIngredient::new,
                new ConditionalDisplay("&a" + AltChar.check + " &7#amount# #item#", "&c" + AltChar.cross + " &7#amount# #item#"),
                nbt -> MythicBukkit.inst().getItemManager().isMythicItem(nbt.getItem()),
                MythicPlayerIngredient::new);
        MMOItems.plugin.getCrafting().registerOutputType("mythic", MythicRecipeOutput::new, "mythicmobs", "mythicmob", "crucible", "crucibles", "mm");

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void b(MythicMechanicLoadEvent event) {

        // Switch Mechanic ig
        switch (event.getMechanicName().toLowerCase()) {
            case "mmoitemsvolley":
                event.register(new MMOItemsArrowVolleyMechanic(event.getContainer().getManager(),
                        event.getContainer().getFile(),
                        event.getConfig().getLine(),
                        event.getConfig()));
                break;
            case "onmmoitemuse":
                event.register(new MMOItemsOnUseAura(event.getContainer().getManager(),
                        event.getContainer().getFile(),
                        event.getConfig().getLine(),
                        event.getConfig()));
                break;
            default:
                break;
        }
    }

    /**
     * MythicLib skill handlers are reloaded on priority {@link EventPriority#NORMAL}
     * MMOCore and MMOItems use HIGH or HIGHEST
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void a(MythicReloadedEvent event) {

        // Update skills
        MMOItems.plugin.getSkills().initialize(true);

        // Update faction stats
        reloadFactionStats();

        // Reload the abilities of online players...
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData data = PlayerData.get(p);
            data.resolveInventory();
        }
    }

    @EventHandler
    public void b(ServerLoadEvent event) {
        reloadFactionStats();
    }

    private static void reloadFactionStats() {

        // Unregister faction stats
        MMOItems.plugin.getStats().unregisterIf(stat -> stat instanceof FactionDamage);

        // Register new faction damage stats
        for (String faction : getFactions()) MMOItems.plugin.getStats().register(new FactionDamage(faction));
    }

    // Using a set to kill duplicates
    private static Set<String> getFactions() {
        var allFactions = new HashSet<String>();

        // Collects all mythic mobs + edited vanilla mobs in mythic mobs.
        for (var mob : MythicBukkit.inst().getMobManager().getVanillaTypes())
            if (mob.hasFaction()) allFactions.add(mob.getFaction().toUpperCase());
        for (var mob : MythicBukkit.inst().getMobManager().getMobTypes())
            if (mob.hasFaction()) allFactions.add(mob.getFaction().toUpperCase());

        return allFactions;
    }
}
