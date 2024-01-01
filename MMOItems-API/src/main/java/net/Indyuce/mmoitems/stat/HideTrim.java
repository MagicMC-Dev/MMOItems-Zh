package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import net.Indyuce.mmoitems.util.VersionDependant;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * @deprecated Merge with other Hide- stats
 */
@Deprecated
@VersionDependant(minor = 20)
public class HideTrim extends BooleanStat {
    public HideTrim() {
        super("HIDE_ARMOR_TRIM", Material.LEATHER_CHESTPLATE, "隐藏装备装饰", new String[]{"隐藏物品 Lore 标注中的盔甲装饰"}, new String[]{"armor"});
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull BooleanData data) {
        if (data.isEnabled()) item.getMeta().addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        if (mmoitem.getNBT().getItem().getItemMeta().hasItemFlag(ItemFlag.HIDE_ARMOR_TRIM))
            mmoitem.setData(ItemStats.HIDE_ARMOR_TRIM, new BooleanData(true));
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
