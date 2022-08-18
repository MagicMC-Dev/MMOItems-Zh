package net.Indyuce.mmoitems.api.item.mmoitem;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class VolatileMMOItem extends ReadMMOItem {

    /**
     * VolatileMMOItems only loads the item data it needs instantly. The item
     * data is only read when using hasData(ItemStat) for the first time.
     * LiveMMOItems read everything on the constructor. VolativeMMOItems are
     * used in player inventory updates.
     * <p>
     * Basically, use this to <b>quickly read Stat Data values</b> from an ItemStack.
     * Since 6.7.5 you can no longer build a volatile MMOItem. It also does NOT load
     * stat histories anymore.
     * <p>
     * If you are editing the stats, and then building a new item stack,
     * you must use {@link LiveMMOItem}.
     *
     * @param item The item to read from
     */
    public VolatileMMOItem(NBTItem item) {
        super(item);
    }

    /**
     * This should only be used once if we want the best performance. This
     * method both checks for stat data, and loads it if it did found one
     *
     * @return If the item has some stat data
     */
    @Override
    public boolean hasData(@NotNull ItemStat stat) {
        if (!super.hasData(stat))

            // Attempt to load this stat data
            try {
                stat.whenLoaded(this);

                // Nope
            } catch (RuntimeException exception) {

                // Log a warning
                MMOItems.plugin.getLogger().log(Level.WARNING,
                        ChatColor.GRAY + "Could not load stat '"
                                + ChatColor.GOLD + stat.getId() + ChatColor.GRAY + "'item data from '"
                                + ChatColor.RED + getId() + ChatColor.GRAY + "': "
                                + ChatColor.YELLOW + exception.getMessage());
            }

        return super.hasData(stat);
    }

    @NotNull
    @Override
    public ItemStackBuilder newBuilder() {
        throw new UnsupportedOperationException("Cannot build a VolatileMMOItem");
    }
}
