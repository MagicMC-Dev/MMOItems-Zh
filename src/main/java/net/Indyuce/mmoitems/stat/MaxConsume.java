package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import net.Indyuce.mmoitems.stat.type.DynamicLoreStat;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class MaxConsume extends DoubleStat implements DynamicLoreStat {
    public MaxConsume() {
        super("MAX_CONSUME", Material.BLAZE_POWDER, "Max Consume", new String[]{"Max amount of usage before", "item disappears."}, new String[]{"consumable"});
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
        int left = (int) ((DoubleData) data).getValue();

        item.addItemTag(new ItemTag(getNBTPath(), left));

        String format = MMOItems.plugin.getLanguage().getStatFormat("max-consume").replace("#", "" + left);
        item.getLore().insert("max-consume", format);
    }

    @Override
    public String getDynamicLoreId() {
        return "max-consume";
    }

    @Override
    public String calculatePlaceholder(NBTItem item) {
        int left = item.getInteger("MMOITEMS_MAX_CONSUME");
        return MMOItems.plugin.getLanguage().getStatFormat("max-consume").replace("#", "" + left);
    }
}
