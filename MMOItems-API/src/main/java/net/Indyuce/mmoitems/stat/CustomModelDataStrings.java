package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.annotation.VersionDependant;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@VersionDependant(version = {1, 21, 4})
public class CustomModelDataStrings extends StringListStat implements GemStoneStat {
    public CustomModelDataStrings() {
        super("CUSTOM_MODEL_DATA_STRINGS", Material.PAINTING, "Custom Model Data Strings", new String[]{"Strings for your custom model data. Consider using", "this over custom model data in 1.21.4+"}, new String[0]);
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringListData data) {
        if (data.getList().isEmpty()) return;

        CustomModelDataComponent comp = item.getMeta().getCustomModelDataComponent();
        comp.setStrings(data.getList());
        item.getMeta().setCustomModelDataComponent(comp);
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        final ItemMeta meta = mmoitem.getNBT().getItem().getItemMeta();
        CustomModelDataComponent comp = meta.getCustomModelDataComponent();
        List<String> strings = comp.getStrings();

        if (strings == null || strings.isEmpty()) return;

        mmoitem.setData(this, new StringListData(strings));
    }
}
