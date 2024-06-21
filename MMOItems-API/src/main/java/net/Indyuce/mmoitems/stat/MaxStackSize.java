package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.util.VersionDependant;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

@VersionDependant(version = {1, 20, 5})
public class MaxStackSize extends DoubleStat implements GemStoneStat {
    public MaxStackSize() {
        super("MAX_STACK_SIZE", Material.CHEST, "可堆叠数", new String[]{"可以堆叠的最大物品数量(1.20.5+)，最大值为99"}, new String[]{"all"});
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {

        // Edit meta
        item.getMeta().setMaxStackSize((int) Math.max(1, Math.min(99, data.getValue())));

        // Save in NBT
        item.addItemTag(getAppliedNBT(data));
    }

    @Override
    public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull DoubleData currentData, @NotNull NumericStatFormula templateData) throws IllegalArgumentException {
        whenApplied(item, currentData);
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull DoubleData data) {

        // Make new ArrayList
        ArrayList<ItemTag> ret = new ArrayList<>();

        // Add Integer
        ret.add(new ItemTag(getNBTPath(), (int) data.getValue()));

        // Return thay
        return ret;
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Get Relevant tags
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.INTEGER));

        // Attempt to build data
        StatData data = getLoadedNBT(relevantTags);

        // Success?
        if (data != null) {
            mmoitem.setData(this, data);
        }
    }

    @Nullable
    @Override
    public DoubleData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        // Find Tag
        ItemTag cmd = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found?
        if (cmd != null) {

            // Well thats it
            return new DoubleData((Integer) cmd.getValue());
        }

        return null;

    }
}
