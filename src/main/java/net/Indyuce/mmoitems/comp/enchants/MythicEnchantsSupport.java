package net.Indyuce.mmoitems.comp.enchants;

import io.lumine.mythicenchants.MythicEnchants;
import io.lumine.mythicenchants.enchants.MythicEnchant;
import io.lumine.mythicenchants.util.LoreParser;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class MythicEnchantsSupport implements EnchantPlugin<MythicEnchant> {

    /*public void reparseWeapon(AbstractPlayer player) {
        MythicEnchantsHelper.reparseWeapon(player);
    }*/

    @Override
    public boolean isCustomEnchant(Enchantment enchant) {
        return enchant instanceof MythicEnchant;
    }

    @Override
    public NamespacedKey getNamespacedKey(String key) {
        return new NamespacedKey(MythicEnchants.inst(), key);
    }

    /**
     * Complete copy and paste of the class {@link MythicEnchant#applyToItem(ItemStack, int)}
     * because that method takes as parameter a fully generated item and updates its meta.
     * Since meta is being generated in parallel to the itemStack in MMOItems, we need
     * to update its lore manually.
     */
    public void handleEnchant(ItemStackBuilder builder, MythicEnchant enchant, int level) {
        Validate.isTrue(level > 0, "Level must be strictly positive");

        // Type cannot be changed. Must make sure that item is an enchanted book

        if (!builder.getMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS))
            builder.getLore().insert(0, LoreParser.formatEnchantment(enchant, level));

        if (builder.getItemStack().getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta meta = (EnchantmentStorageMeta) builder.getMeta();

            if (!builder.getMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS))
                builder.getLore().insert(0, LoreParser.formatEnchantment(enchant, level));

           /* lvl = (Integer)this.getEnchantManager().getMythicEnchants(item).getOrDefault(this, 0);
            if (lvl > 0) {
                ((List)lore).remove(LoreParser.formatEnchantment(this, lvl));
            }*/

            // Now handled in the Enchants item stat
            // meta.addStoredEnchant(this, level, true);
        } else {
            /*lvl = (Integer)this.getEnchantManager().getMythicEnchants(item).getOrDefault(this, 0);
            if (lvl > 0) {
                ((List)lore).remove(LoreParser.formatEnchantment(this, lvl));
            }*/

            if (!builder.getMeta().hasItemFlag(ItemFlag.HIDE_ENCHANTS))
                builder.getLore().insert(0, LoreParser.formatEnchantment(enchant, level));

            // Now handled in the Enchants stat
            // item.addUnsafeEnchantment(this, level);
        }
    }
}
