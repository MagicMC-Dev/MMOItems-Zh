package net.Indyuce.mmoitems.stat;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.version.VersionMaterial;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CompatibleMaterials extends ItemStat<StringListData, StringListData> {

    public CompatibleMaterials() {
        super("COMPATIBLE_MATERIALS", VersionMaterial.COMMAND_BLOCK.toMaterial(), "Compatible Materials",
                new String[]{"The item materials this skin is", "compatible with."}, new String[]{"skin"});
    }

    @Override
    @SuppressWarnings("unchecked")
    public StringListData whenInitialized(Object object) {
        Validate.isTrue(object instanceof List<?>, "Must specify a string list");
        return new StringListData((List<String>) object);
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new StatEdition(inv, ItemStats.COMPATIBLE_MATERIALS).enable("Write in the chat the name of the material you want to add.");

        if (event.getAction() != InventoryAction.PICKUP_HALF || !inv.getEditedSection().contains("compatible-materials"))
            return;
        List<String> lore = inv.getEditedSection().getStringList("compatible-materials");
        if (lore.size() < 1)
            return;

        String last = lore.get(lore.size() - 1);
        lore.remove(last);
        inv.getEditedSection().set("compatible-materials", lore);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "Successfully removed '" + last + "'.");
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        final Player player = inv.getPlayer();
        // Check if material exists
        if (Arrays.stream(Material.values()).noneMatch(versionMaterial -> versionMaterial.name().equalsIgnoreCase(message))) {
            player.sendMessage(MMOItems.plugin.getPrefix() + "Invalid material name.");
            return;
        }

        List<String> lore = inv.getEditedSection().contains("compatible-materials") ? inv.getEditedSection().getStringList("compatible-materials")
                : new ArrayList<>();
        lore.add(message.toUpperCase());
        inv.getEditedSection().set("compatible-materials", lore);
        inv.registerTemplateEdition();
        player.sendMessage(MMOItems.plugin.getPrefix() + "Compatible Materials successfully added.");
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<StringListData> statData) {
        if (statData.isPresent()) {
            lore.add(ChatColor.GRAY + "Current Value:");
            statData.get().getList().forEach(str -> lore.add(ChatColor.GRAY + str));
        } else
            lore.add(ChatColor.GRAY + "Current Value: " + ChatColor.RED + "Compatible with any material.");

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Click to add a new material.");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " Right click to remove the last material.");
    }

    @NotNull
    @Override
    public StringListData getClearStatData() {
        return new StringListData();
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringListData data) {
        // Copy Array, for lore
        List<String> compatibleMaterials = new ArrayList<>(data.getList());
        item.getLore().insert("compatible-materials", compatibleMaterials);

        // Add data
        item.addItemTag(getAppliedNBT(data));
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StringListData data) {
        // Build Json Array
        JsonArray array = new JsonArray();

        // For each string in the ids of the data
        for (String sts : data.getList()) {
            array.add(sts);
        }

        // Make returning array
        ArrayList<ItemTag> tags = new ArrayList<>();

        // Add Json Array
        tags.add(new ItemTag(getNBTPath(), array.toString()));

        return tags;
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        // FInd relevant tags
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

        // Generate data
        StatData data = getLoadedNBT(relevantTags);

        if (data != null)
            mmoitem.setData(this, data);
    }

    @Nullable
    @Override
    public StringListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        // Find relevant tag
        ItemTag rTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found?
        if (rTag == null)
            // Nope
            return null;

        try {
            // Parse onto Json Array
            JsonArray array = new JsonParser().parse((String) rTag.getValue()).getAsJsonArray();

            // Make and return list
            return new StringListData(array);
        } catch (JsonSyntaxException | IllegalStateException exception) {
            /*
             * OLD ITEM WHICH MUST BE UPDATED.
             */
        }
        return null;
    }
}
