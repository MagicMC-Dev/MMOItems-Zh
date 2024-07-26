package net.Indyuce.mmoitems.comp.enchants;

import io.lumine.mythicenchants.MythicEnchants;
import io.lumine.mythicenchants.enchants.MythicEnchant;
import io.lumine.mythicenchants.util.LoreParser;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MythicEnchantsSupport implements EnchantPlugin<MythicEnchant> {
    private final MythicEnchants manager = (MythicEnchants) Bukkit.getPluginManager().getPlugin("MythicEnchants");

    @NotNull
    @Override
    public String getNamespace() {
        return "mythicenchants";
    }

    @Nullable
    @Override
    public MythicEnchant transfer(@NotNull Enchantment enchantment) {
        return manager.getEnchantManager().toMythicEnchantment(enchantment).orElse(null);
    }

    public void handleEnchant(@NotNull ItemStackBuilder builder, @NotNull MythicEnchant enchant, int level) {

        // Type cannot be changed. Must make sure that item is an enchanted book
        if (!builder.getMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS))
            builder.getLore().insert(0, LoreParser.formatEnchantment(enchant, level));
    }
}
