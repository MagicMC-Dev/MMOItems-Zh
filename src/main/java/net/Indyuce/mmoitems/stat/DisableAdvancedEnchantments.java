package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.stat.type.DisableStat;
import net.advancedplugins.ae.api.EnchantApplyEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DisableAdvancedEnchantments extends DisableStat implements Listener {
    public DisableAdvancedEnchantments() {
        super("ADVANCED_ENCHANTS", Material.ENCHANTED_BOOK, "Disable Advanced Enchants", new String[]{"all"}, "When toggled on, prevents players", "from applying AE onto this item.");

        if (Bukkit.getPluginManager().getPlugin("AdvancedEnchantments") == null)
            disable();
    }

    @EventHandler
    public void a(EnchantApplyEvent event) {
        if (NBTItem.get(event.getItem()).getBoolean(getNBTPath()))
            event.setCancelled(true);
    }
}
