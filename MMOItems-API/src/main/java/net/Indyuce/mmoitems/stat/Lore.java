package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.GemStoneStat;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Lore extends StringListStat implements GemStoneStat {
    public Lore() {
        super("LORE", VersionMaterial.WRITABLE_BOOK.toMaterial(), "Lore", new String[]{"The item lore."}, new String[]{"all"});
    }

    @Override
    @SuppressWarnings("unchecked")
    public StringListData whenInitialized(Object object) {
        Validate.isTrue(object instanceof List<?>, "Must specify a string list");
        return new StringListData((List<String>) object);
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringListData data) {

        // Apply yes
        item.addItemTag(getAppliedNBT(data));
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new StatEdition(inv, ItemStats.LORE).enable("Write in the chat the lore line you want to add.");

        if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().contains("lore")) {
            List<String> lore = inv.getEditedSection().getStringList("lore");
            if (lore.isEmpty())
                return;

            String last = lore.get(lore.size() - 1);
            lore.remove(last);
            inv.getEditedSection().set("lore", lore.isEmpty() ? null : lore);
            inv.registerTemplateEdition();
            inv.getPlayer()
                    .sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed '" + MythicLib.plugin.parseColors(last) + ChatColor.GRAY + "'.");
        }
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        List<String> lore = inv.getEditedSection().contains("lore") ? inv.getEditedSection().getStringList("lore") : new ArrayList<>();
        lore.add(message);
        inv.getEditedSection().set("lore", lore);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Lore successfully added.");
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<StringListData> statData) {
        statData.ifPresentOrElse(stringListData -> {
            lore.add(ChatColor.GRAY + "Current Value:");
            lore.addAll(MythicLib.plugin.parseColors(stringListData.getList()
                    .stream()
                    .map(s -> ChatColor.GRAY + s)
                    .toList()));
        }, () -> lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None"));

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a line.");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last line.");
    }

    /*
     * The lore is not directly inserted into the final itemStack lore
     * because all stats have not registered all their lore placeholders
     * yet. The lore is only saved in a JSon array so that it can be
     * recalculated LATER on with right placeholders
     */
}
