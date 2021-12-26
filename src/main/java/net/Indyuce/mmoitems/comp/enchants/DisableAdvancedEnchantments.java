package net.Indyuce.mmoitems.comp.enchants;

import net.Indyuce.mmoitems.stat.type.DisableStat;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class DisableAdvancedEnchantments extends DisableStat {
    public DisableAdvancedEnchantments() {

        super("ADVANCED_ENCHANTS", Material.ENCHANTED_BOOK, "Disable Advanced Enchants", new String[]{"all"}, "When toggled on, prevents players", "from applying AE onto this item.");
    }
}
