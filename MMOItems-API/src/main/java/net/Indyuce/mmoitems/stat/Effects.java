package net.Indyuce.mmoitems.stat;

import com.google.gson.*;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.PotionEffectData;
import net.Indyuce.mmoitems.stat.data.PotionEffectListData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectData;
import net.Indyuce.mmoitems.stat.data.random.RandomPotionEffectListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.PlayerConsumable;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Effects extends ItemStat<RandomPotionEffectListData, PotionEffectListData> implements PlayerConsumable {
    public Effects() {
        super("EFFECTS", Material.POTION, "效果", new String[]{"药水会影响你的消耗品的效果"},
                new String[]{"consumable"});
    }

    @Override
    public RandomPotionEffectListData whenInitialized(Object object) {
        Validate.isTrue(object instanceof ConfigurationSection, "必须指定配置部分");
        return new RandomPotionEffectListData((ConfigurationSection) object);
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new StatEdition(inv, ItemStats.EFFECTS).enable("在聊天中输入你想要添加的永久药水效果",
                    ChatColor.AQUA + "格式: {药水效果名称}|{持续时间数值公式}|{增幅数值公式}", ChatColor.DARK_RED + "Note: " + ChatColor.RED + " '|' 行是字面意思");

        if (event.getAction() == InventoryAction.PICKUP_HALF) {
            if (inv.getEditedSection().contains("effects")) {
                Set<String> set = inv.getEditedSection().getConfigurationSection("effects").getKeys(false);
                String last = Arrays.asList(set.toArray(new String[0])).get(set.size() - 1);
                inv.getEditedSection().set("effects." + last, null);
                if (set.size() <= 1)
                    inv.getEditedSection().set("effects", null);
                inv.registerTemplateEdition();
                inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + " 已成功删除 " + last.substring(0, 1).toUpperCase()
                        + last.substring(1).toLowerCase() + ChatColor.GRAY);
            }
        }
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        String[] split = message.split("\\|");
        Validate.isTrue(split.length > 1, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "使用此格式: $e{药水效果名称}|{持续时间数值公式}|{增幅数值公式}$b"));

        PotionEffectType effect = PotionEffectType.getByName(split[0].replace("-", "_").replace(" ", "_").toUpperCase());
        Validate.notNull(effect, split[0] + FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), " 不是有效的药水效果, 所有药水效果都可以在这里找到:$e https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/potion/PotionEffectType.html"));

        NumericStatFormula duration = new NumericStatFormula(split[1]);
        NumericStatFormula amplifier = split.length > 2 ? new NumericStatFormula(split[2]) : new NumericStatFormula(1, 0, 0, 0);

        duration.fillConfigurationSection(inv.getEditedSection(), "effects." + effect.getName() + ".duration");
        amplifier.fillConfigurationSection(inv.getEditedSection(), "effects." + effect.getName() + ".amplifier");
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + effect.getName() + " " + amplifier + "添加成功");
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<RandomPotionEffectListData> statData) {
        statData.ifPresentOrElse(randomPotionEffectListData -> {
            lore.add(ChatColor.GRAY + "当前值: ");
            for (RandomPotionEffectData effect : randomPotionEffectListData.getEffects())
                lore.add(ChatColor.GRAY + "* " + ChatColor.GREEN + UtilityMethods.caseOnWords(effect.getType().getName().toLowerCase().replace("_", " "))
                        + ChatColor.GRAY + " 等级: " + ChatColor.GREEN + effect.getAmplifier() + ChatColor.GRAY + " 持续时间: " + ChatColor.GREEN
                        + effect.getDuration());
        }, () -> lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "None"));
        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 单击以添加效果");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击以删除最后一个效果");
    }

    @NotNull
    @Override
    public PotionEffectListData getClearStatData() {
        return new PotionEffectListData();
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull PotionEffectListData data) {
        // Process Lore
        List<String> lore = new ArrayList<>();
        String effectFormat = getGeneralStatFormat();
        data.getEffects().forEach(effect -> lore.add(effectFormat
                .replace("{effect}",
                        MMOItems.plugin.getLanguage().getPotionEffectName(effect.getType())
                                + (effect.getLevel() < 2 ? "" : " " + MMOUtils.intToRoman(effect.getLevel())))
                .replace("{duration}", MythicLib.plugin.getMMOConfig().decimal.format(effect.getDuration()))));
        item.getLore().insert("effects", lore);

        // Add tags to item
        item.addItemTag(getAppliedNBT(data));
    }

    @Override
    public String getLegacyTranslationPath() {
        return "effect";
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull PotionEffectListData data) {

        // Create a Json Array
        JsonArray array = new JsonArray();

        // For every effect
        for (PotionEffectData effect : data.getEffects()) {
            // Convert to Json Object
            JsonObject object = new JsonObject();
            object.addProperty("Type", effect.getType().getName());
            object.addProperty("Duration", effect.getDuration());
            object.addProperty("Level", effect.getLevel());
            array.add(object);
        }

        // Make the tag
        ArrayList<ItemTag> ret = new ArrayList<>();
        ret.add(new ItemTag(getNBTPath(), array.toString()));

        // Thats it
        return ret;
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        // Find relevant tag
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING));

        // Attempt to build data
        StatData data = getLoadedNBT(relevantTags);

        // Valid? Append.
        if (data != null)
            mmoitem.setData(this, data);
    }

    @Nullable
    @Override
    public PotionEffectListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        // Find tag
        ItemTag rTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found?
        if (rTag == null)
            return null;
        // Must be Json Array, attempt to parse.
        try {
            PotionEffectListData effects = new PotionEffectListData();

            // Get as Array
            JsonArray array = new JsonParser().parse((String) rTag.getValue()).getAsJsonArray();

            // BUild each element
            for (JsonElement e : array) {

                // Must be object
                if (e.isJsonObject()) {

                    // Extract
                    JsonObject key = e.getAsJsonObject();

                    effects.add(new PotionEffectData(PotionEffectType.getByName(
                            key.get("Type").getAsString()),
                            key.get("Duration").getAsDouble(),
                            key.get("Level").getAsInt()));
                }
            }

            // Success
            return effects;

        } catch (JsonSyntaxException | IllegalStateException exception) {
            /*
             * OLD ITEM WHICH MUST BE UPDATED.
             */
        }
        return null;
    }

    @Override
    public void onConsume(@NotNull VolatileMMOItem mmo, @NotNull Player player, boolean vanillaEating) {
        // Does it have effects?
        if (!mmo.hasData(ItemStats.EFFECTS))
            return;

        // Get Data
        PotionEffectListData pelData = (PotionEffectListData) mmo.getData(ItemStats.EFFECTS);

        // Apply
        for (PotionEffectData ped : pelData.getEffects()) {
            if (ped == null)
                continue;
            player.removePotionEffect(ped.getType());
            player.addPotionEffect(ped.toEffect());
        }
    }
}
