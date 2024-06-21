package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import org.bukkit.Material;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Item damage is the item's vanilla damage. This is not the item's current
 * durability, these two values are linked by the following formula:
 * `max_durability = durability + damage`
 *
 * @author indyuce
 * @implNote Due to 1.20.5+ modifying the location of many vanilla tags,
 * all version-friendly implementations must use ItemMeta methods.
 */
public class ItemDamage extends DoubleStat implements GemStoneStat {
    public ItemDamage() {
        super("ITEM_DAMAGE", Material.FISHING_ROD, "物品伤害(最大攻击力)",
                new String[]{"默认物品伤害 &c这不会", "影响物品的最大耐久度"}, new String[]{"!block", "all"});
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {
        if (item.getMeta() instanceof Damageable)
            ((Damageable) item.getMeta()).setDamage((int) data.getValue());
    }

    @Override
    public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull DoubleData currentData, @NotNull NumericStatFormula templateData) throws IllegalArgumentException {
        whenApplied(item, currentData);
    }

    /**
     * This stat is saved not as a custom tag, but as the vanilla HideFlag itself.
     * Alas this is an empty array
     */
    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull DoubleData data) {
        return new ArrayList<>();
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        if (mmoitem.getNBT().getItem().getItemMeta() instanceof Damageable)
            mmoitem.setData(ItemStats.ITEM_DAMAGE, new DoubleData(((Damageable) mmoitem.getNBT().getItem().getItemMeta()).getDamage()));
    }

    /**
     * This stat is saved not as a custom tag, but as the vanilla HideFlag itself.
     * Alas this method returns null.
     */
    @Nullable
    @Override
    public DoubleData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        return null;
    }

    @Override
    public String getLegacyTranslationPath() {
        return "durability";
    }
}
