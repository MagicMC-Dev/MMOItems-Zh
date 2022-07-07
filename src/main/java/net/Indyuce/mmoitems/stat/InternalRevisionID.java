package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InternalRevisionID extends ItemStat<RandomStatData<StatData>, StatData> implements InternalStat {
    public InternalRevisionID() {
        super("INTERNAL_REVISION_ID", Material.ITEM_FRAME, "Internal Revision ID", new String[] { "The Internal Revision ID is used to determine",
                        "if an item is outdated or not. You", "should increase this whenever", "you make changes to your item!"},
                new String[] { "all" });
    }

    @Nullable
    @Override
    public RandomStatData whenInitialized(Object object) {
        // not supported
        return null;
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        // not supported
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        // not supported
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<RandomStatData<StatData>> statData) {
        // not supported
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
