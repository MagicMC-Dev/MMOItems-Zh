package net.Indyuce.mmoitems.comp.enchants;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

public interface EnchantPlugin<T extends Enchantment> {

    /**
     * @param enchant Enchant being checked
     * @return If this enchant plugin handles a given enchant
     */
    public boolean isCustomEnchant(Enchantment enchant);

    /**
     * Called when an item is built. This should be used to add the enchantment
     * lines to the item lore or add any item tag required by the enchantment.
     *
     * @param builder Item being built
     * @param enchant Enchantment being applied
     * @param level   Enchant level
     */
    public void handleEnchant(ItemStackBuilder builder, T enchant, int level);

    public NamespacedKey getNamespacedKey(String key);
}
