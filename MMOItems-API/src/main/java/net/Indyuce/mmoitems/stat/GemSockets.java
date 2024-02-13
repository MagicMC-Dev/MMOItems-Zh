package net.Indyuce.mmoitems.stat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.GemSocketsData;
import net.Indyuce.mmoitems.stat.data.GemstoneData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GemSockets extends ItemStat<GemSocketsData, GemSocketsData> {
    public GemSockets() {
        super("GEM_SOCKETS", Material.EMERALD, "宝石插槽", new String[]{"武器拥有的宝石插槽数量"},
                new String[]{"weapon", "catalyst", "tool", "armor", "accessory", "!gem_stone"});
    }

    @Override
    @SuppressWarnings("unchecked")
    public GemSocketsData whenInitialized(Object object) {
        Validate.isTrue(object instanceof List<?>, "必须指定一个字符串列表");
        return new GemSocketsData((List<String>) object);
    }

    private String emptyGemSocketFormat, filledGemSocketFormat;

    @Override
    public void loadConfiguration(@NotNull ConfigurationSection legacyLanguageFile, @NotNull Object configObject) {

        // LEGACY CODE
        if (configObject instanceof String) {
            emptyGemSocketFormat = legacyLanguageFile.getString("empty-gem-socket");
            filledGemSocketFormat = legacyLanguageFile.getString("filled-gem-socket");
        }

        // Up-to-date code
        else {
            Validate.isTrue(configObject instanceof ConfigurationSection, "Must be a config section");
            final ConfigurationSection config = (ConfigurationSection) configObject;
            emptyGemSocketFormat = config.getString("empty");
            filledGemSocketFormat = config.getString("filled");
        }
    }

    @Override
    public String getLegacyTranslationPath() {
        return "empty-gem-socket";
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull GemSocketsData sockets) {

        // Append NBT Tags
        item.addItemTag(getAppliedNBT(sockets));

        // Edit Lore
        List<String> lore = new ArrayList<>();
        for (GemstoneData gem : sockets.getGemstones()) {
            String gemName = gem.getName();

            // Upgrades?
            if (item.getMMOItem().hasUpgradeTemplate()) {

                int iLvl = item.getMMOItem().getUpgradeLevel();
                if (iLvl != 0) {

                    Integer gLvl = gem.getLevel();

                    if (gLvl != null) {

                        int dLevel = iLvl - gLvl;

                        gemName = DisplayName.appendUpgradeLevel(gemName, dLevel);
                    }
                }
            }

            lore.add(filledGemSocketFormat.replace("{name}", gemName));
        }
        sockets.getEmptySlots().forEach(slot -> lore.add(emptyGemSocketFormat.replace("{name}", slot)));
        item.getLore().insert("gem-stones", lore);
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull GemSocketsData sockets) {

        // Well its just a Json tostring
        ArrayList<ItemTag> ret = new ArrayList<>();
        ret.add(new ItemTag(getNBTPath(), sockets.toJson().toString()));

        // Thats it
        return ret;
    }

    @Override
    @NotNull
    public String getNBTPath() {
        return "MMOITEMS_GEM_STONES";
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Find relevant tags
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

        // Attempt to build
        StatData data = getLoadedNBT(relevantTags);

        // Valid?
        if (data != null) {
            mmoitem.setData(this, data);
        }
    }

    @Nullable
    @Override
    public GemSocketsData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        // Find Tag
        ItemTag gTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found?
        if (gTag != null) {

            try {
                // Interpret as Json Object
                JsonObject object = new JsonParser().parse((String) gTag.getValue()).getAsJsonObject();
                GemSocketsData sockets = new GemSocketsData(object.getAsJsonArray("EmptySlots"));

                JsonArray array = object.getAsJsonArray("Gemstones");
                array.forEach(element -> sockets.add(new GemstoneData(element.getAsJsonObject())));

                // Return built
                return sockets;

            } catch (JsonSyntaxException | IllegalStateException exception) {
                /*
                 * OLD ITEM WHICH MUST BE UPDATED.
                 */
            }
        }

        // Nope
        return null;
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new StatEdition(inv, ItemStats.GEM_SOCKETS).enable("在聊天中输入您要添加的宝石插槽的颜色。");

        if (event.getAction() == InventoryAction.PICKUP_HALF) {
            if (inv.getEditedSection().contains(getPath())) {
                List<String> lore = inv.getEditedSection().getStringList("" + getPath());
                if (lore.size() < 1)
                    return;

                String last = lore.get(lore.size() - 1);
                lore.remove(last);
                inv.getEditedSection().set("" + getPath(), lore);
                inv.registerTemplateEdition();
                inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "成功删除 '" + last + ChatColor.GRAY + "'.");
            }
        }
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        List<String> lore = inv.getEditedSection().contains(getPath()) ? inv.getEditedSection().getStringList("" + getPath()) : new ArrayList<>();
        lore.add(message);
        inv.getEditedSection().set("" + getPath(), lore);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + message + " 添加成功");
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<GemSocketsData> statData) {

        if (statData.isPresent()) {
            lore.add(ChatColor.GRAY + "当前值: ");
            GemSocketsData data = statData.get();
            data.getEmptySlots().forEach(socket -> lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + socket + " 宝石插槽"));

        } else
            lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "无插槽");

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 左键以添加宝石插槽");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键以删除宝石插槽");
    }

    @NotNull
    @Override
    public GemSocketsData getClearStatData() {
        return new GemSocketsData(new ArrayList<>());
    }
}
