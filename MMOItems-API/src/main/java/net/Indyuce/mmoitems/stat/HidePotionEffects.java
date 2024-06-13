package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.BooleanData;
import net.Indyuce.mmoitems.stat.type.BooleanStat;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @deprecated Merge with other Hide- stats
 */
@Deprecated
public class HidePotionEffects extends BooleanStat {
	public HidePotionEffects() {
		super("HIDE_POTION_EFFECTS", Material.POTION, "隐藏药水效果", new String[]{"隐藏物品 Lore 标注中的药水效果和 '无效果'"}, new String[0], Material.POTION, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.TIPPED_ARROW);
	}

    @BackwardsCompatibility(version = "1.20.5")
    public static final ItemFlag ITEM_FLAG = UtilityMethods.resolveEnumField(ItemFlag::valueOf,
            () -> Arrays.stream(ItemFlag.values()).findAny().get(),
            "HIDE_ADDITIONAL_TOOLTIP", "HIDE_POTION_EFFECTS");

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull BooleanData data) {
        if (data.isEnabled()) item.getMeta().addItemFlags(ITEM_FLAG);
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        if (mmoitem.getNBT().getItem().getItemMeta().hasItemFlag(ITEM_FLAG))
            mmoitem.setData(ItemStats.HIDE_POTION_EFFECTS, new BooleanData(true));
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
