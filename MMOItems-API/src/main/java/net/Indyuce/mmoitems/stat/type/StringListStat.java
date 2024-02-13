package net.Indyuce.mmoitems.stat.type;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import net.Indyuce.mmoitems.stat.data.type.StatData;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StringListStat extends ItemStat<StringListData, StringListData> {
    public StringListStat(String id, Material mat, String name, String[] lore, String[] types, Material... materials) {
        super(id, mat, name, lore, types, materials);
    }

    @Override
    @SuppressWarnings("unchecked")
    public StringListData whenInitialized(Object object) {
        Validate.isTrue(object instanceof List<?>, "必须指定一个字符串列表");
        return new StringListData((List<String>) object);
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringListData data) {

        // Empty stuff
        if (!(data instanceof StringListData)) return;
        if (data.getList().size() == 0) return;

        // Chop
        final String joined = String.join(", ", data.getList());
        final String format = getGeneralStatFormat();
        final String finalStr = format.replace("{value}", joined);

        // Display in lore
        // item.getLore().insert(getPath(), SilentNumbers.chop(finalStr, 50, col.toString()));
        item.getLore().insert(getPath(), finalStr);
        item.addItemTag(getAppliedNBT(data));
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StringListData data) {

        // Start out with a new JSON Array
        JsonArray array = new JsonArray();

        // For every list entry
        for (String str : data.getList()) {

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
            new StatEdition(inv, this).enable("在聊天中输入您要添加的行");

        if (event.getAction() == InventoryAction.PICKUP_HALF && inv.getEditedSection().contains(getPath())) {
            List<String> list = inv.getEditedSection().getStringList(getPath());
            if (list.isEmpty())
                return;

            String last = list.get(list.size() - 1);
            list.remove(last);
            inv.getEditedSection().set(getPath(), list.isEmpty() ? null : list);
            inv.registerTemplateEdition();
            inv.getPlayer()
                    .sendMessage(MMOItems.plugin.getPrefix() + "成功删除 '" + MythicLib.plugin.parseColors(last) + ChatColor.GRAY + "'.");
        }
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        List<String> list = inv.getEditedSection().contains(getPath()) ? inv.getEditedSection().getStringList(getPath()) : new ArrayList<>();
        list.add(message);
        inv.getEditedSection().set(getPath(), list);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + "统计添加成功");
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
        if (data != null) {
            mmoitem.setData(this, data);
        }
    }

    @Nullable
    @Override
    public StringListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        // Get it
        ItemTag listTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found?
        if (listTag != null) {
            try {

                // Value must be a Json Array
                String[] array = new Gson().fromJson((String) listTag.getValue(), String[].class);

                // Create String List Data
                return new StringListData(array);

            } catch (JsonSyntaxException | IllegalStateException exception) {
                /*
                 * OLD ITEM WHICH MUST BE UPDATED.
                 */
            }
        }

        // No correct tags
        return null;
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<StringListData> statData) {
        if (statData.isPresent()) {
            lore.add(ChatColor.GRAY + "当前值: ");
            StringListData data = statData.get();
            data.getList().forEach(element -> lore.add(ChatColor.GRAY + MythicLib.plugin.parseColors(element)));

        } else
            lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "None");

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 单击以添加权限");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击删除最后一个权限");
    }

    @NotNull
    @Override
    public StringListData getClearStatData() {
        return new StringListData();
    }
}
