package net.Indyuce.mmoitems.comp.enchants;

import net.Indyuce.mmoitems.stat.type.DisableStat;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class DisableAdvancedEnchantments extends DisableStat {
    public DisableAdvancedEnchantments() {

        super("ADVANCED_ENCHANTS", Material.ENCHANTED_BOOK, "禁用 Advanced Enchants", new String[]{"all"}, " 开启后, 会阻止玩家", "将 AE 应用于此物品");
    }
}
