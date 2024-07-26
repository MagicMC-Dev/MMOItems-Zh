package net.Indyuce.mmoitems.comp.enchants;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * There are three types of enchant plugins.
 * - enchants saved using the Bukkit Enchantment interface (EcoEnchants, MythicEnchants)
 * - enchants saved in the NBT (AdvancedEnchants)
 * - enchants saved in lore only (CrazyEnchants)
 * <p>
 * Interface used to support plugins which use the Bukkit Enchantment
 * interface to register their enchantments. This makes enchant storage
 * so much easier for MMOItems.
 */
public interface EnchantPlugin<T> {

    /**
     * Used to determine if an enchantment comes from that enchant plugin
     *
     * @return The namespace of keys used for registering Bukkit enchantments
     */
    @NotNull
    String getNamespace();

    @Nullable
    T transfer(@NotNull Enchantment enchant);

    /**
     * Called when an item is built. This should be used to add the enchantment
     * lines to the item lore or add any item tag required by the enchantment.
     *
     * @param builder Item being built
     * @param enchant Enchantment being applied
     * @param level   Enchant level
     */
    void handleEnchant(@NotNull ItemStackBuilder builder, @NotNull T enchant, int level);

    @NotNull
    default NamespacedKey getNamespacedKey(@NotNull String key) {
        return new NamespacedKey(getNamespace(), key);
    }
}
