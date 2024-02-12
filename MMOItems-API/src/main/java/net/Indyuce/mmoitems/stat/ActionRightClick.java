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
        super("ON_RIGHT_CLICK", Material.COMMAND_BLOCK_MINECART, "Right Click Action", new String[]{"ID of skill ran when right clicking. When used,", "The item will naturally apply item costs like", "mana, stamina, cooldown. This option overrides the", "script provided by the item type."}, new String[]{"weapon"});
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        final String format = UtilityMethods.enumName(message);
        MythicLib.plugin.getSkills().getHandlerOrThrow(format);
        inv.getEditedSection().set(getPath(), format);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + " successfully changed to '" + format + "'");
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        item.addItemTag(new ItemTag(getNBTPath(), UtilityMethods.enumName(data.toString())));
        // No lore insertion
    }
}

