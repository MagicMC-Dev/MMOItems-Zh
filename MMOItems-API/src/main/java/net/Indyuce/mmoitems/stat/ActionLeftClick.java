package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.StringStat;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ActionLeftClick extends StringStat {
    public ActionLeftClick() {
        super("ON_LEFT_CLICK", Material.COMMAND_BLOCK_MINECART, "左键活动", new String[]{"左键时运行的技能ID。在使用时，", "物品将自然应用项目成本，如法力、耐力和冷却。", "此选项将覆盖物品类型提供的脚本。"}, new String[]{"weapon"});

        // Staff spirit set as alias
        setAliases(LEGACY_ID);
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

    public static final String LEGACY_ID = "STAFF_SPIRIT";
    public static final String LEGACY_PATH = "MMOITEMS_" + LEGACY_ID;

    // TODO refactor with stat categories
    @Override
    @Deprecated
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        ArrayList<ItemTag> relevantTags = new ArrayList<>();

        // New path
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

        // Legacy path
        if (mmoitem.getNBT().hasTag(LEGACY_PATH))
            relevantTags.add(new ItemTag(getNBTPath(), ItemTag.getTagAtPath(LEGACY_PATH, mmoitem.getNBT(), SupportedNBTTagValues.STRING).getValue()));

        StringData bakedData = getLoadedNBT(relevantTags);
        if (bakedData != null) mmoitem.setData(this, bakedData);
    }
}

