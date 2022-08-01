package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.StringData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Stats are marked externals when refering to item templates options:
 * - {@link net.Indyuce.mmoitems.stat.BrowserDisplayIDX}
 * - {@link}
 * <p>
 * These stats do not save any information in the item NBT
 */
public interface TemplateOption {

    /**
     * This stat is not saved onto items. This method is empty.
     */
    public default void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        // Cannot throw an exception since it will be called
    }

    /**
     * This stat is not saved onto items. This method always returns null
     */
    @Nullable
    public default StringData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        throw new RuntimeException("Not supported");
    }

    /**
     * This stat is not saved onto items. This method is empty.
     */
    public default void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        throw new RuntimeException("Not supported");
    }

    /**
     * This stat is not saved onto items. This method returns an empty array.
     */
    @NotNull
    public default ArrayList<ItemTag> getAppliedNBT(@NotNull StringData data) {
        throw new RuntimeException("Not supported");
    }
}
