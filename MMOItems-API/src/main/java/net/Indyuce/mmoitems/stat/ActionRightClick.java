package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class ActionRightClick extends StringStat {
    public ActionRightClick() {
        super("ON_RIGHT_CLICK", Material.COMMAND_BLOCK_MINECART, "右键活动", new String[]{"右键时运行的技能ID。在使用时，", "物品将自然应用项目成本，如法力、耐力和冷却。", "此选项将覆盖物品类型提供的脚本。"}, new String[]{"weapon"});
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        final String format = UtilityMethods.enumName(message);
        MythicLib.plugin.getSkills().getHandlerOrThrow(format);
        inv.getEditedSection().set(getPath(), format);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + "成功设置为 '" + format + "'");
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        item.addItemTag(new ItemTag(getNBTPath(), UtilityMethods.enumName(data.toString())));
        // No lore insertion
    }
}

