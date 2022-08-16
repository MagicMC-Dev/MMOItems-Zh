package net.Indyuce.mmoitems.comp.enchants;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

/**
 * There are three types of enchant plugins.
 * - enchants saved using the Bukkit Enchantment interface (EcoEnchants, MythicEnchants)
 * - enchants saved in the NBT (AdvancedEnchants)
 * - enchants saved in lore only (CrazyEnchants)
 * <p>
 * Interface used to support plugins which use the Bukkit Enchantment
 * interface to register their enchantments. This makes enchant storage
 * so much easier for MMOItems.
 *
 * @param <T> The plugin class implementing Enchantment
 */
public interface EnchantPlugin<T extends Enchantment> {

    /**
     * @param enchant Enchant being checked
     * @return If this enchant plugin handles a given enchant
     */
    boolean isCustomEnchant(Enchantment enchant);

    /**
     * Called when an item is built. This should be used to add the enchantment
     * lines to the item lore or add any item tag required by the enchantment.
     *
     * @param builder Item being built
     * @param enchant Enchantment being applied
     * @param level   Enchant level
     */
    void handleEnchant(ItemStackBuilder builder, T enchant, int level);

    NamespacedKey getNamespacedKey(String key);
}
