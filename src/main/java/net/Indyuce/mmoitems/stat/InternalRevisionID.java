package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.InternalStat;
import net.mmogroup.mmolib.api.item.ItemTag;
import org.bukkit.Material;

public class InternalRevisionID extends InternalStat {
    public InternalRevisionID() {
        super("INTERNAL_REVISION_ID", Material.ITEM_FRAME, "Internal Revision ID", new String[] { "The Internal Revision ID is used to determine",
                        "if an item is outdated or not. You", "should increase this whenever", "you make changes to your item!"},
                new String[] { "all" });
    }

    @Override
    public void whenApplied(ItemStackBuilder item, StatData data) {
        item.addItemTag(new ItemTag(getNBTPath(), MMOItems.INTERNAL_REVISION_ID));
    }

    @Override
    public void whenLoaded(ReadMMOItem mmoitem) {
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            mmoitem.setData(this, new DoubleData(mmoitem.getNBT().getInteger(getNBTPath())));
    }
}
