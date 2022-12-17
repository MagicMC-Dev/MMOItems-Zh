package net.Indyuce.mmoitems.api.item.util;


import io.lumine.mythic.lib.util.AdventureUtils;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Slightly different from the dynamic lore. Instead of saving in the item NBT
 * the lore with everything parsed except the dynamic part, we just look for
 * the last line and replace it with the new one.
 * <p>
 * This class is only made to make editing the item lore easier, it does not
 * update the item NBT corresponding to the stat being edited. That has to
 * be done in parallel when using this class.
 * <p>
 * Currently this is being used to display custom durability, consumable
 * uses that are left, as well as tool experience and levels
 *
 * @author indyuce using arias initial code
 */
public class LoreUpdate {
    private final ItemStack item;
    private final String old, replace;
    private final List<String> lore;

    /**
     * Used to handle live lore updates.
     *
     * @param item    The NBTItem to update
     * @param old     The old lore line that needs to be replaced
     * @param replace The new lore line
     */
    public LoreUpdate(ItemStack item, String old, String replace) {
        this.item = item;
        this.old = old;
        this.replace = replace;
        this.lore = item.getItemMeta().getLore();
    }

    public ItemStack updateLore() {

        // Possible that the item has no lore
        if (lore == null || lore.isEmpty())
            return item;

        for (int i = 0; i < lore.size(); i++) {

            /*
             * Finds the old line in the old lore.
             *
             * There is this weird issue where when generating the item
             * and getting its lore again via the Bukkit ItemMeta, color
             * codes are now UPPERCASE, which make the strings not match
             * anymore unless we use equalsIgnoreCase().
             *
             * In theory, equals() would have been sufficient.
             */
            if (lore.get(i).equalsIgnoreCase(old)) {
                lore.set(i, replace);

                ItemMeta meta = item.getItemMeta();
                AdventureUtils.setLore(meta, lore);
                item.setItemMeta(meta);

                return item;
            }
        }

        /*
         * If the program reaches this then the old lore
         * was removed by another plugin or something.
         */
        return item;
        /*throw new NoSuchElementException("Could not find old lore line; item lore not updated");*/
    }
}
