package net.Indyuce.mmoitems.comp.mmoinventory.stat;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringStat;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.version.VersionMaterial;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class AccessorySet extends StringStat implements GemStoneStat {
    public AccessorySet() {
        super("ACCESSORY_SET", VersionMaterial.OAK_SIGN.toMaterial(), "配件套装 (MMOInventory)", new String[] { "使用 MMOInventory 的独特限制", "即每套装备只允许装备一个配件" }, new String[] { "!block", "all" });
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        item.addItemTag(new ItemTag(getNBTPath(), data.toString()));
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        message = message.toLowerCase();
        inv.getEditedSection().set(getPath(), message);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(
                MMOItems.plugin.getPrefix() + getName() + "成功更改为" + MythicLib.plugin.parseColors(message) + ChatColor.GRAY + "");
    }
}
