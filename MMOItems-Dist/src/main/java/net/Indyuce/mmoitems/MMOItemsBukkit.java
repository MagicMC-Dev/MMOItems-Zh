package net.Indyuce.mmoitems;

import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmoitems.api.player.PlayerData;
import net.Indyuce.mmoitems.comp.PhatLootsHook;
import net.Indyuce.mmoitems.gui.listener.GuiListener;
import net.Indyuce.mmoitems.listener.*;
import net.Indyuce.mmoitems.listener.option.DroppedItems;
import org.bukkit.Bukkit;

public class MMOItemsBukkit {

    /**
     * Called when MMOItems enables. This registers
     * all the listeners required for MMOItems to run
     */
    public MMOItemsBukkit(MMOItems plugin) {

        Bukkit.getPluginManager().registerEvents(new ItemUse(), plugin);
        Bukkit.getPluginManager().registerEvents(new ItemListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new CustomSoundListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new DurabilityListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new DisableInteractions(), plugin);
        Bukkit.getPluginManager().registerEvents(new GuiListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new BiomeChangeListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new CustomBlockListener(), plugin);
        if (Bukkit.getPluginManager().getPlugin("PhatLoots") != null)
            Bukkit.getPluginManager().registerEvents(new PhatLootsHook(), plugin);

        if (plugin.getConfig().getBoolean("dropped-items.tier-glow") || plugin.getConfig().getBoolean("dropped-items.hints"))
            Bukkit.getPluginManager().registerEvents(new DroppedItems(plugin.getConfig().getConfigurationSection("dropped-items")), plugin);
        if (plugin.getLanguage().disableRemovedItems)
            Bukkit.getPluginManager().registerEvents(new DisabledItemsListener(plugin), plugin);

        // Profile support
        if (MythicLib.plugin.hasProfiles())
            Bukkit.getPluginManager().registerEvents(new ProfileSupportListener(), plugin);

        Bukkit.getScheduler().runTaskTimer(plugin, () -> Bukkit.getOnlinePlayers().forEach(player -> PlayerData.get(player).timedRunnable()), 100, 20);
    }
}
