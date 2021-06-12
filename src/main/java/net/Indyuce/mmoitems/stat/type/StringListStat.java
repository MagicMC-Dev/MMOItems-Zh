package net.Indyuce.mmoitems.stat.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import net.Indyuce.mmoitems.stat.data.type.StatData;

public class StringListStat extends ItemStat {
    public StringListStat(String id, Material mat, String name, String[] lore, String[] types, Material... materials) {
        super(id, mat, name, lore, types, materials);
    }

    @Override
    @SuppressWarnings("unchecked")
    public StringListData whenInitialized(Object object) {
        Validate.isTrue(object instanceof List<?>, "Must specify a string list");
        return new StringListData((List<String>) object);
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StatData data) {
        item.addItemTag(getAppliedNBT(data));
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StatData data) {

        // Start out with a new JSON Array
        JsonArray array = new JsonArray();

        // For every list entry
        for (String str : ((StringListData) data).getList()) {

            // Add to the array as-is
            array.add(str);
        }

        // Make the result list
        ArrayList<ItemTag> ret = new ArrayList<>();

        // Add the Json Array
        ret.add(new ItemTag(getNBTPath(), array.toString()));

        // Ready.
        return ret;
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new StatEdition(inv, this).enable("Write in the chat the line you want to add.");

        if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().contains(getPath())) {
            List<String> list = inv.getEditedSection().getStringList(getPath());
            if (list.isEmpty())
                return;

            String last = list.get(list.size() - 1);
            list.remove(last);
            inv.getEditedSection().set(getPath(), list.isEmpty() ? null : list);
            inv.registerTemplateEdition();
            inv.getPlayer()
                    .sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed '" + MythicLib.plugin.parseColors(last) + ChatColor.GRAY + "'.");
        }
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        List<String> list = inv.getEditedSection().contains(getPath()) ? inv.getEditedSection().getStringList(getPath()) : new ArrayList<>();
        list.add(message);
        inv.getEditedSection().set(getPath(), list);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + " Stat successfully added.");
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Find the relevant tags
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

        // Generate data
        StatData data = getLoadedNBT(relevantTags);

        // Valid?
        if (data != null) { mmoitem.setData(this, data); }
    }

    @Nullable
    @Override
    public StatData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        // Get it
        ItemTag listTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found?
        if (listTag != null) {
            try {

                // Value must be a Json Array
                JsonArray array = new JsonParser().parse((String) listTag.getValue()).getAsJsonArray();

                // Create String List Data
                return new StringListData(array);

            } catch (JsonSyntaxException |IllegalStateException exception) {
                /*
                 * OLD ITEM WHICH MUST BE UPDATED.
                 */
            }
        }

        // No correct tags
        return null;
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<RandomStatData> statData) {
        if (statData.isPresent()) {
            lore.add(ChatColor.GRAY + "Current Value:");
            StringListData data = (StringListData) statData.get();
            data.getList().forEach(element -> lore.add(ChatColor.GRAY + MythicLib.plugin.parseColors(element)));

        } else
            lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "None");

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a permission.");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last permission.");
    }

    @NotNull
    @Override
    public StatData getClearStatData() {
        return new StringListData();
    }
}
