package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.StatFormat;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class InternalRevisionID extends InternalStat {
    public InternalRevisionID() {
        super("INTERNAL_REVISION_ID", Material.ITEM_FRAME, "Internal Revision ID", new String[] { "The Internal Revision ID is used to determine",
                        "if an item is outdated or not. You", "should increase this whenever", "you make changes to your item!"},
                new String[] { "all" });
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
        item.addItemTag(getAppliedNBT(data));
    }

    @Override
    public @NotNull ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) {

        // Create Fresh
        ArrayList<ItemTag> ret = new ArrayList<>();

        // Add sole tag
        ret.add(new ItemTag(getNBTPath(), MMOItems.INTERNAL_REVISION_ID));

        // Return thay
        return ret;
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Get tags
        ArrayList<ItemTag> relevantTags = new ArrayList<>();

        // Add sole tag
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.DOUBLE));

        // Use that
        StatData bakedData = getLoadedNBT(relevantTags);

        // Valid?
        if (bakedData != null) {

            // Set
            mmoitem.setData(this, bakedData);
        }
    }

    @Override
    public @Nullable StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        // You got a double righ
        ItemTag tg = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found righ
        if (tg != null) {

            // Get number
            Double value = (Double) tg.getValue();

            // Thats it
            return new DoubleData(value);
        }

        // Fail
        return null;
    }

    @NotNull
    @Override
    public StatData getClearStatData() {
        return new DoubleData(MMOItems.INTERNAL_REVISION_ID);
    }
}
