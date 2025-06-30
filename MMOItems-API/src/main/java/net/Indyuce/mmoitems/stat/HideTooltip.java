package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.annotation.VersionDependant;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @deprecated Merge with other Hide- stats
 */
@Deprecated
@VersionDependant(version = {1, 20, 5})
public class HideTooltip extends BooleanStat {
    public HideTooltip() {
        super("HIDE_TOOLTIP", Material.ACACIA_SIGN, "Hide Tooltip", new String[]{"Completely the hides item tooltip,", "if that's what you wanna do.", "", "Available only on 1.20.5+"}, new String[0]);
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull BooleanData data) {
        if (data.isEnabled())
            item.getMeta().setHideTooltip(true);
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        if (mmoitem.getNBT().getItem().getItemMeta().isHideTooltip())
            mmoitem.setData(this, new BooleanData(true));
    }

    /**
     * This stat is saved not as a custom tag, but as the vanilla HideFlag itself.
     * Alas this is an empty array
     */
    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull BooleanData data) {
        return new ArrayList<>();
    }

    /**
     * This stat is saved not as a custom tag, but as the vanilla HideFlag itself.
     * Alas this method returns null.
     */
    @Nullable
    @Override
    public BooleanData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        return null;
    }
}
