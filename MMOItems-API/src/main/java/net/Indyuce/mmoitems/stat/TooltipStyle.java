package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.stat.annotation.VersionDependant;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

@VersionDependant(version = {1, 21, 2})
public class TooltipStyle extends StringStat implements GemStoneStat {
    public TooltipStyle() {
        super("TOOLTIP_STYLE", Material.ACACIA_SIGN, "原版 Tooltip 样式", new String[]{"您物品的原版 Tooltips 样式，", "仅在 1.21.2 及以上版本可用"}, new String[0]);
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        NamespacedKey resolved = NamespacedKey.fromString(data.getString());
        item.getMeta().setTooltipStyle(resolved);
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        final ItemMeta meta = mmoitem.getNBT().getItem().getItemMeta();
        if (!meta.hasTooltipStyle()) return;

        final NamespacedKey namesp = meta.getTooltipStyle();
        mmoitem.setData(this, new StringData(namesp.toString()));
    }
}
