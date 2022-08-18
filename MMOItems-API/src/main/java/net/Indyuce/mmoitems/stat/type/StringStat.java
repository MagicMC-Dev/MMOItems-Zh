package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StringStat extends ItemStat<StringData, StringData> {
    public StringStat(String id, Material mat, String name, String[] lore, String[] types, Material... materials) {
        super(id, mat, name, lore, types, materials);
    }

    @Override
    public StringData whenInitialized(Object object) {
        return new StringData(object.toString());
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {

        // Add NBT
        item.addItemTag(getAppliedNBT(data));

        // Display in lore
        item.getLore().insert(getPath(), data.toString());
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StringData data) {

        // Make Array
        ArrayList<ItemTag> ret = new ArrayList<>();

        // Add that tag
        ret.add(new ItemTag(getNBTPath(), data.toString()));

        // Thats it
        return ret;
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_HALF) {
            inv.getEditedSection().set(getPath(), null);
            inv.registerTemplateEdition();
            inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed " + getName() + ".");
        } else
            new StatEdition(inv, this).enable("Write in the chat the text you want.");
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        inv.getEditedSection().set(getPath(), message);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(
                MMOItems.plugin.getPrefix() + getName() + " successfully changed to '" + MythicLib.plugin.parseColors(message) + ChatColor.GRAY + "'.");
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Get tags
        ArrayList<ItemTag> relevantTags = new ArrayList<>();

        // Add sole tag
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

        // Use that
        StringData bakedData = getLoadedNBT(relevantTags);

        // Valid?
        if (bakedData != null) {

            // Set
            mmoitem.setData(this, bakedData);
        }
    }

	@Nullable
    @Override
    public StringData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        // You got a double righ
        ItemTag tg = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found righ
        if (tg != null) {

            // Get number
            String value = (String) tg.getValue();

            // Thats it
            return new StringData(value);
        }

        // Fail
        return null;
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<StringData> statData) {
        if (statData.isPresent()) {
            String value = MythicLib.plugin.parseColors(statData.get().toString());
            value = value.length() > 40 ? value.substring(0, 40) + "..." : value;
            lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.GREEN + value);

        } else
            lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Left click to change this value.");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove this value.");
    }

    @NotNull
    @Override
    public StringData getClearStatData() {
        return new StringData("");
    }
}
