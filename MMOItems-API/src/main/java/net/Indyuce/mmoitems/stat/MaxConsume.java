package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class MaxConsume extends DoubleStat {
    public MaxConsume() {
        super("MAX_CONSUME", Material.BLAZE_POWDER, "Max Consume", new String[]{"Max amount of usage before", "item disappears."}, new String[]{"consumable"});
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {
        int left = (int) data.getValue();

        item.addItemTag(new ItemTag(getNBTPath(), left));

        String format = MMOItems.plugin.getLanguage().getStatFormat("max-consume").replace("{value}", String.valueOf(left));
        item.getLore().insert("max-consume", format);
    }
}
