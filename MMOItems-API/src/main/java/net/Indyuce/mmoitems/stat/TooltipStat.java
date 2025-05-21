package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.tooltip.TooltipTexture;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TooltipStat extends StringStat implements GemStoneStat {
    public TooltipStat() {
        super("TOOLTIP", Material.BIRCH_SIGN, "Custom Tooltip", new String[]{"您要使用的自定义工具提示纹理的标识符。", "使用方法请查看维基百科！"}, new String[0]);
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        final String format = UtilityMethods.enumName(data.toString());
        final @Nullable TooltipTexture texture = MMOItems.plugin.getLore().getTooltip(format);
        Validate.notNull(texture, "找不到 ID 为 '" + format + "' 的 Tooltip");
        item.addItemTag(new ItemTag("MMOITEMS_TOOLTIP", texture.getId()));
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        final String format = UtilityMethods.enumName(message);
        Validate.isTrue(MMOItems.plugin.getLore().hasTooltip(format), "找不到 ID 为 '" + format + "' 的工具提示");

        inv.getEditedSection().set(getPath(), format);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "已成功更改为 " + format + ".");
    }
}
