package net.Indyuce.mmoitems.comp.enchants;

import io.lumine.mythicenchants.MythicEnchants;
import io.lumine.mythicenchants.enchants.MythicEnchant;
import io.lumine.mythicenchants.util.LoreParser;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;

public class MythicEnchantsSupport implements EnchantPlugin<MythicEnchant> {

    @Override
    public boolean isCustomEnchant(Enchantment enchant) {
        return enchant instanceof MythicEnchant;
    }

    @Override
    public NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(MythicEnchants.inst(), key);
    }

    public void handleEnchant(ItemStackBuilder builder, MythicEnchant enchant, int level) {
        Validate.isTrue(level > 0, "Level must be strictly positive");

        // Type cannot be changed. Must make sure that item is an enchanted book

        if (!builder.getMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS))
            builder.getLore().insert(0, LoreParser.formatEnchantment(enchant, level));
    }
}
