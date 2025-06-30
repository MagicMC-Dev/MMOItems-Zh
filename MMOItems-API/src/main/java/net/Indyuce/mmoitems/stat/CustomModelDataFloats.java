package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.annotation.VersionDependant;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

@VersionDependant(version = {1, 21, 4})
public class CustomModelDataFloats extends StringListStat implements GemStoneStat {
    public CustomModelDataFloats() {
        super("CUSTOM_MODEL_DATA_FLOATS", Material.PAINTING, "Custom Model Data Floats", new String[]{"Floats for your custom model data. Consider using", "this over custom model data in 1.21.4+"}, new String[0]);
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringListData data) {
        if (data.getList().isEmpty()) return;

        var comp = item.getMeta().getCustomModelDataComponent();
        comp.setFloats(data.getList().stream().map(Float::parseFloat).toList());
        item.getMeta().setCustomModelDataComponent(comp);
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        var meta = mmoitem.getNBT().getItem().getItemMeta();
        var comp = meta.getCustomModelDataComponent();
        var strings = comp.getFloats();

        if (strings.isEmpty()) return;

        mmoitem.setData(this, new StringListData(strings.stream().map(String::valueOf).toList()));
    }
}
