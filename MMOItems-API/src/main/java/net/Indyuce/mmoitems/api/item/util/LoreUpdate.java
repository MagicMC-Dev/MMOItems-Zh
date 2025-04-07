package net.Indyuce.mmoitems.api.item.util;


import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.util.AdventureUtils;
import net.Indyuce.mmoitems.ItemStats;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

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
    private final ItemMeta meta;
    private final String pattern, replace;
    private final List<String> lore;

    @ApiStatus.Experimental
    private final boolean hasTooltip;

    /**
     * Used to handle live lore updates.
     *
     * @param item    The NBTItem to update
     * @param pattern The old lore line that needs to be replaced
     * @param replace The new lore line
     */
    public LoreUpdate(ItemStack item, @Nullable ItemMeta meta, @Nullable NBTItem nbtItem, String pattern, String replace) {
        this.item = item;
        this.meta = meta == null ? item.getItemMeta() : meta;
        this.replace = replace;
        this.pattern = pattern.toLowerCase();
        this.lore = item.getItemMeta().getLore();
        this.hasTooltip = nbtItem.hasTag(ItemStats.TOOLTIP.getNBTPath());
    }

    @Nullable
    private String getResult(String line, String pattern) {

        if (hasTooltip) {
            final int index = line.toLowerCase().indexOf(pattern);
            if (index == -1) return null;
            // Replace substring with new one
            return line.substring(0, index) + replace + line.substring(index + pattern.length());
        }

        /*
         * There is this weird issue where when generating the item
         * and getting its lore again via the Bukkit ItemMeta, color
         * codes are now UPPERCASE, which make the strings not match
         * anymore unless we use equalsIgnoreCase().
         */
        return line.equalsIgnoreCase(pattern) ? replace : null;
    }

    public ItemStack updateLore() {

        // If item has no lore
        if (lore == null || lore.isEmpty()) return item;

        for (int i = 0; i < lore.size(); i++) {
            String lineResult = getResult(lore.get(i), pattern);
            if (lineResult != null) {
                lore.set(i, lineResult);

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
