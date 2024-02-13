package net.Indyuce.mmoitems.stat;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.PlusMinusPercent;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.comp.enchants.EnchantPlugin;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.EnchantListData;
import net.Indyuce.mmoitems.stat.data.random.RandomEnchantListData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.Indyuce.mmoitems.stat.type.StatHistory;
import net.Indyuce.mmoitems.stat.type.Upgradable;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Enchants extends ItemStat<RandomEnchantListData, EnchantListData> implements Upgradable {
    public Enchants() {
        super("ENCHANTS", Material.ENCHANTED_BOOK, "附魔", new String[]{"物品附魔"}, new String[]{"all"});
    }

    @Override
    public RandomEnchantListData whenInitialized(Object object) {
        Validate.isTrue(object instanceof ConfigurationSection, "必须指定配置部分");
        return new RandomEnchantListData((ConfigurationSection) object);
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_ALL)
            new StatEdition(inv, ItemStats.ENCHANTS).enable("在聊天中输入您要添加的附魔",
                    ChatColor.AQUA + "格式: {附魔名称} {附魔等级数值公式}");

        if (event.getAction() == InventoryAction.PICKUP_HALF) {
            if (inv.getEditedSection().contains("enchants")) {
                Set<String> set = inv.getEditedSection().getConfigurationSection("enchants").getKeys(false);
                String last = Arrays.asList(set.toArray(new String[0])).get(set.size() - 1);
                inv.getEditedSection().set("enchants." + last, null);
                if (set.size() <= 1)
                    inv.getEditedSection().set("enchants", null);
                inv.registerTemplateEdition();
                inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "已成功删除" + last.substring(0, 1).toUpperCase()
                        + last.substring(1).toLowerCase().replace("_", " ") + ".");
            }
        }
    }

    /*
     * getByName is deprecated, but it's safe to
     * use and will make the user experience better
     */
    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        String[] split = message.split(" ");
        Validate.isTrue(split.length >= 2, "使用这种格式: {Enchant Name} {Enchant Level Numeric Formula}. 示例: 'sharpness 5 0.3' "
                + "代表 Sharpness 5, 每个物品级别加上 0.3 级别 (向上舍入到较小的整数) ");

        Enchantment enchant = getEnchant(split[0]);
        Validate.notNull(enchant, split[0]
                + " 不是有效的附魔! 所有附魔都可以在这里找到: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/enchantments/Enchantment.html");

        NumericStatFormula formula = new NumericStatFormula(message.substring(message.indexOf(" ") + 1));
        formula.fillConfigurationSection(inv.getEditedSection(), "enchants." + enchant.getKey().getKey());
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + enchant.getKey().getKey() + " " + formula.toString() + " successfully added.");
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<RandomEnchantListData> statData) {

        if (statData.isPresent()) {
            lore.add(ChatColor.GRAY + "当前值: ");
            RandomEnchantListData data = statData.get();
            data.getEnchants().forEach(enchant -> lore.add(ChatColor.GRAY + "* " + UtilityMethods.caseOnWords(enchant.getKey().getKey().replace("_", " "))
                    + " " + data.getLevel(enchant).toString()));

        } else
            lore.add(ChatColor.GRAY + "当前值: " + ChatColor.RED + "None");

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 单击以添加附魔");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击删除最后一个附魔");
    }

    @NotNull
    @Override
    public EnchantListData getClearStatData() {
        return new EnchantListData();
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Create enchant data from this items' enchantments
        EnchantListData enchants = new EnchantListData();

        // Get the Item Meta
        ItemStack item = mmoitem.getNBT().getItem();
        if (item.hasItemMeta()) {

            // Finally, item meta
            ItemMeta itemMeta = item.getItemMeta();
            if (itemMeta != null) {

                // For each enchantment, in the usual way
                for (Enchantment enchant : itemMeta.getEnchants().keySet()) {

                    // Add Level
                    enchants.addEnchant(enchant, itemMeta.getEnchantLevel(enchant));
                }

                // For each enchantment 'stored' in the item
                if (itemMeta instanceof EnchantmentStorageMeta) {

                    // For each enchantment
                    for (Enchantment enchant : ((EnchantmentStorageMeta) itemMeta).getStoredEnchants().keySet()) {

                        // Add Level
                        enchants.addEnchant(enchant, ((EnchantmentStorageMeta) itemMeta).getStoredEnchantLevel(enchant));
                    }
                }
            }
        }

        // Recognize the Stat Data
        mmoitem.setData(ItemStats.ENCHANTS, enchants);

        // Regardless, the stat history must be kept.
        ItemTag hisTag = ItemTag.getTagAtPath(ItemStackBuilder.history_keyword + getId(), mmoitem.getNBT(), SupportedNBTTagValues.STRING);
        if (hisTag == null) {

            if (// If either enchanting is allowed
                    !mmoitem.getNBT().getBoolean(ItemStats.DISABLE_ENCHANTING.getNBTPath()) ||

                            // Or repairing is allowed
                            !mmoitem.getNBT().getBoolean(ItemStats.DISABLE_REPAIRING.getNBTPath())) {

                /*
                 *   Capture the stat history of enchantments of this item.
                 *
                 *   If the item has no enchantments, it will record that as the
                 *   original value of the item, which can later be assumed to be
                 *   true to the template.
                 *
                 *   todo for a few months, we must not assume that the 'Original'
                 *    stat values are actually the original ones, they may be old
                 *    enchantments applied by players before StatHistory existed.
                 *
                 */
                StatHistory.from(mmoitem, ItemStats.ENCHANTS);
            }
        }
    }

    /**
     * @param source Something that may not even have enchs.
     *
     * @return Enchantments of this extracted as a list of enchants data.
     */
    @NotNull public static EnchantListData fromVanilla(@Nullable ItemStack source) {

        // List
        EnchantListData eld = new EnchantListData();

        // Null is clear
        if (source == null) { return eld; }

        // For each enchants
        for (Enchantment e : source.getEnchantments().keySet()) {

            // Get level
            int l = source.getEnchantmentLevel(e);

            // Add if significant
            if (l != 0) { eld.addEnchant(e, l); }
        }

        return eld;
    }


    /**
     * Since GemStones shall be removable, the enchantments must also be stored.
     */
    @Nullable
    @Override
    public EnchantListData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        // Find tag
        ItemTag enchantTag = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found?
        if (enchantTag != null) {

            // Must be thay string list shit
            ArrayList<String> enchants = ItemTag.getStringListFromTag(enchantTag);

            // New
            EnchantListData data = new EnchantListData();

            // Examine each
            for (String str : enchants) {

                // Split
                String[] split = str.split(" ");
                if (split.length >= 2) {

                    // Find
                    String enchantment = split[0];
                    String level = split[1];

                    // Get Namespaced
                    Enchantment ench = null;
                    try {
                        ench = getEnchant(enchantment);
                    } catch (Exception ignored) {
                    }

                    // Parse Integer
                    Integer lvl = SilentNumbers.IntegerParse(level);

                    // Worked?
                    if (ench != null && lvl != null) {

                        // Add
                        data.addEnchant(ench, lvl);
                    }
                }
            }

            // Thats it
            return data;
        }

        return null;
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull EnchantListData enchants) {

        for (Enchantment enchant : enchants.getEnchants()) {
            int lvl = enchants.getLevel(enchant);

            // If it's an enchanted item, has to be registered as a stored enchant instead
            if (item.getItemStack().getType() == Material.ENCHANTED_BOOK) {

                // Vanilla enchanted books expect this behaviour from enchants I guess
                ((EnchantmentStorageMeta) item.getMeta()).addStoredEnchant(enchant, lvl, true);

            // Add normally
            } else { item.getMeta().addEnchant(enchant, lvl, true); }

            // Handle custom enchant
            for (EnchantPlugin enchantPlugin : MMOItems.plugin.getEnchantPlugins())
                if (enchantPlugin.isCustomEnchant(enchant))
                    enchantPlugin.handleEnchant(item, enchant, lvl);
        }

        // Apply tags
        item.addItemTag(getAppliedNBT(enchants));
    }

    /**
     * Since GemStones shall be removable, the enchantments must also be stored.
     */
    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull EnchantListData data) {
        ArrayList<ItemTag> ret = new ArrayList<>();

        // Add enchantment pair data
        ArrayList<String> enchantments = new ArrayList<>();
        for (Enchantment enchantment : data.getEnchants()) {
            enchantments.add(enchantment.getKey().getKey() + " " + data.getLevel(enchantment));
        }

        // Add that one tag
        ret.add(ItemTag.fromStringList(getNBTPath(), enchantments));

        return ret;
    }

    @NotNull
    @Override
    public UpgradeInfo loadUpgradeInfo(@Nullable Object obj) throws IllegalArgumentException {
        //UPGRD//MMOItems. Log("\u00a7a  --> \u00a77Loading Enchants");
        return EnchantUpgradeInfo.GetFrom(obj);
    }

    @NotNull
    @Override
    public StatData apply(@NotNull StatData original, @NotNull UpgradeInfo info, int level) {
        //UPGRD//MMOItems. Log("\u00a7a  --> \u00a77Applying Enchants Upgrade");

        // Must be DoubleData
        if (original instanceof EnchantListData && info instanceof EnchantUpgradeInfo) {
            //UPGRD//MMOItems. Log("\u00a7a   -> \u00a77Valid Instances");

            // Get value
            EnchantListData dataEnchants = ((EnchantListData) original);
            EnchantUpgradeInfo eui = (EnchantUpgradeInfo) info;

            // For every enchantment
            for (Enchantment e : eui.getAffectedEnchantments()) {
                int lSimulation = level;
                //UPGRD//MMOItems. Log("\u00a7b    >\u00a79> \u00a77Enchantment \u00a7f" + e.getName());

                // Get current level
                double value = dataEnchants.getLevel(e);
                //UPGRD//MMOItems. Log("\u00a7b      -> \u00a77Original Level \u00a7f" + value);
                PlusMinusPercent pmp = eui.getPMP(e);
                if (pmp == null) {
                    continue;
                }
                //UPGRD//MMOItems. Log("\u00a7b      -> \u00a77Operation \u00a7f" + pmp.toString());

                // If leveling up
                if (lSimulation > 0) {

                    // While still positive
                    while (lSimulation > 0) {

                        // Apply PMP Operation Positively
                        //UPGRD//MMOItems. Log("\u00a7c       -> \u00a77Preop \u00a7f" + value);
                        //UPGRD//MMOItems. Log("\u00a78       -> Operation \u00a77" + pmp.toString());
                        value = pmp.apply(value);
                        //UPGRD//MMOItems. Log("\u00a76       -> \u00a77Postop \u00a7f" + value);
                        //UPGRD//MMOItems. Log("\u00a77       -> \u00a77------------------");

                        // Decrease
                        lSimulation--;
                    }

                    // Degrading the item
                } else if (lSimulation < 0) {

                    // While still negative
                    while (lSimulation < 0) {

                        // Apply PMP Operation Reversibly
                        //UPGRD//MMOItems. Log("\u00a73       -> \u00a77Preop \u00a7f" + value);
                        value = pmp.reverse(value);
                        //UPGRD//MMOItems. Log("\u00a7d       -> \u00a77Postop \u00a7f" + value);
                        //UPGRD//MMOItems. Log("\u00a77       -> \u00a77------------------");

                        // Decrease
                        lSimulation++;
                    }
                }

                // Update
                //UPGRD//MMOItems. Log("\u00a7b      -> \u00a77Final level \u00a7f" + value);
                dataEnchants.addEnchant(e, SilentNumbers.floor(value));
            }

            // Yes
            return dataEnchants;
        }

        // Upgraded
        return original;
    }

    /**
     * Players may enchant an item via 'vanilla' means (Not Gemstones, not Upgrades).
     * If they do so, the enchantments of their item will be wiped if they try to put
     * a gemstone in or upgrade the item, which is not nice.
     * <p></p>
     * This operation classifies these discrepancies in the enchantment levels as
     * 'externals' in the Stat History of enchantments. From then on, they will be
     * accounted for.
     */
    public static void separateEnchantments(@NotNull MMOItem mmoitem) {

        // Cancellation because the player could not have done so HMMM
        if (mmoitem.hasData(ItemStats.DISABLE_REPAIRING) && mmoitem.hasData(ItemStats.DISABLE_ENCHANTING)) {
            return;
        }
        boolean additiveMerge = MMOItems.plugin.getConfig().getBoolean("stat-merging.additive-enchantments", false);
        //SENCH//MMOItems.log(" \u00a79>\u00a73>\u00a77 Separating Enchantments");

        // Does it have enchantment data?
        if (mmoitem.hasData(ItemStats.ENCHANTS)) {

            // Get that data
            EnchantListData data = (EnchantListData) mmoitem.getData(ItemStats.ENCHANTS);
            StatHistory hist = StatHistory.from(mmoitem, ItemStats.ENCHANTS);

            //SENCH//MMOItems.log(" \u00a7b:\u00a73:\u00a79: \u00a77Early Analysis: \u00a73o-o-o-o-o-o-o-o-o-o-o-o-o-o-o-o");
            //SENCH//MMOItems.log("  \u00a73=\u00a7b> \u00a77Active:");
            //SENCH//for (Enchantment e : data.getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + data.getLevel(e)); }

            //SENCH//MMOItems.log("  \u00a73> \u00a77History:");
            //SENCH//MMOItems.log("  \u00a73=\u00a7b> \u00a77Original:");
            //SENCH//for (Enchantment e : ((EnchantListData) hist.getOriginalData()).getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getOriginalData()).getLevel(e)); }
            //SENCH//MMOItems.log("  \u00a73=\u00a7b> \u00a77Stones:");
            //SENCH//for (UUID date : hist.getAllGemstones()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77" + date.toString()); for (Enchantment e : ((EnchantListData) hist.getGemstoneData(date)).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getGemstoneData(date)).getLevel(e)); } }
            //SENCH//MMOItems.log("  \u00a73=\u00a7b> \u00a77Externals:");
            //SENCH//for (StatData date : hist.getExternalData()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77 --------- "); for (Enchantment e : ((EnchantListData) date).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) date).getLevel(e)); } }

            // All right, whats the expected enchantment levels?
            //HSY//MMOItems.log(" \u00a73-\u00a7a- \u00a77Enchantment Separation Recalculation \u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-\u00a73-\u00a7a-");
            EnchantListData expected = (EnchantListData) hist.recalculate(mmoitem.getUpgradeLevel());

            // Gather a list of extraneous enchantments
            HashMap<Enchantment, Integer> discrepancies = new HashMap<>();

            // For every enchantment
            for (Enchantment e : Enchantment.values()) {

                // Get both
                int actual = data.getLevel(e);
                int ideal = expected.getLevel(e);

                // Intuitive additive merge
                if (additiveMerge) {

                    // Get difference, and register.
                    int offset = actual - ideal;
                    if (offset != 0) {
                        discrepancies.put(e, offset);
                        //SENCH//MMOItems.log("\u00a77 Act \u00a7f" + actual + "\u00a77 Ide \u00a73" + ideal + "\u00a77 Off \u00a79" + offset + " \u00a77 -- of \u00a7b" + e.getName() );
                    }

                    // Weird maximum enchantment
                } else {

                    // We can only know that, if the actual is greater than the ideal, there is a greater external.
                    if (actual > ideal) {
                        discrepancies.put(e, actual);
                        //SENCH//MMOItems.log("\u00a77 Act \u00a7f" + actual + "\u00a77 Ide \u00a73" + ideal + " \u00a77 -- of \u00a7b" + e.getName() );
                    }
                }
            }

            // It has been extracted
            if (discrepancies.size() > 0) {
                // Generate enchantment list with offsets
                EnchantListData extraneous = new EnchantListData();
                for (Enchantment e : discrepancies.keySet()) {
                    //SENCH//MMOItems.log("\u00a77 Discrepancy of \u00a7f" + discrepancies.get(e) + " \u00a77 -- in \u00a7b" + e.getName() );

                    extraneous.addEnchant(e, discrepancies.get(e));
                }

                // Register extraneous
                hist.registerExternalData(extraneous);
            }

            //SENCH//MMOItems.log(" \u00a7b:\u00a73:\u00a79: \u00a77Results \u00a79o-o-o-o-o-o-o-o-o-o-o-o-o-o-o-o");
            //SENCH//MMOItems.log("  \u00a73> \u00a77History:");
            //SENCH//MMOItems.log("  \u00a73=\u00a7b> \u00a77Original:");
            //SENCH//for (Enchantment e : ((EnchantListData) hist.getOriginalData()).getEnchants()) { MMOItems.log("  \u00a7b * \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getOriginalData()).getLevel(e)); }
            //SENCH//MMOItems.log("  \u00a73=\u00a7b> \u00a77Stones:");
            //SENCH//for (UUID date : hist.getAllGemstones()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77" + date.toString()); for (Enchantment e : ((EnchantListData) hist.getGemstoneData(date)).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) hist.getGemstoneData(date)).getLevel(e)); } }
            //SENCH//MMOItems.log("  \u00a73=\u00a7b> \u00a77Externals:");
            //SENCH//for (StatData date : hist.getExternalData()) { MMOItems.log("  \u00a7b==\u00a73> \u00a77 --------- "); for (Enchantment e : ((EnchantListData) date).getEnchants()) { MMOItems.log("  \u00a7b    *\u00a73* \u00a77" + e.getName() + " \u00a7f" + ((EnchantListData) date).getLevel(e)); } }

        }
    }

    /**
     * This is useful for custom enchant plugins which
     * utilize the Enchantment bukkit interface.
     *
     * @param key String input which can either be the enchant key or name
     * @return Found bukkit enchantment instance
     */
    @SuppressWarnings("deprecation")
    @Nullable
    public static Enchantment getEnchant(String key) {
        key = key.toLowerCase().replace("-", "_");
        Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(key));

        //  Vanilla enchant
        if (enchant != null)
            return enchant;

        // Check for custom enchants
        for (EnchantPlugin enchPlugin : MMOItems.plugin.getEnchantPlugins()) {
            Enchantment checked = Enchantment.getByKey(enchPlugin.getNamespacedKey(key));
            if (checked != null)
                return checked;
        }

        // Last try, vanilla enchant with name
        return Enchantment.getByName(key);
    }

    public static class EnchantUpgradeInfo implements UpgradeInfo {
        @NotNull
        HashMap<Enchantment, PlusMinusPercent> perEnchantmentOperations = new HashMap<>();

        /**
         * Generate a <code>DoubleUpgradeInfo</code> from this <code><b>String List</b></code>
         * that represents several pairs of {@link Enchantment}-{@link PlusMinusPercent}.
         * <p></p>
         * To keep older MMOItems versions working the same way, instead of having no prefix
         * to use the <i>set</i> function of the PMP, one must use an <b><code>s</code></b> prefix.
         *
         * @param obj A <code><u>String List</u></code> in the format:
         *            <p><code>Enchantment PMP</code>
         *            </p><code>Enchantment PMP</code>
         *            <p><code>Enchantment PMP</code>
         *            </p><code>...</code>
         * @throws IllegalArgumentException If any part of the operation goes wrong (including reading any PMP).
         */
        @NotNull
        public static EnchantUpgradeInfo GetFrom(@Nullable Object obj) throws IllegalArgumentException {

            // Shall not be null
            Validate.notNull(obj, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "升级操作列表不能为空"));

            // Does the string exist?
            if (!(obj instanceof List)) {

                // Throw exception
                throw new IllegalArgumentException(
                        FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "需要字符串列表而不是 $i{0}", obj.toString()));
            }

            ArrayList<String> strlst = new ArrayList<>();
            boolean failure = false;
            StringBuilder unreadableStatements = new StringBuilder();
            for (Object entry : (List) obj) {

                // Only strings
                if (entry instanceof String) {
                    strlst.add((String) entry);

                } else {

                    // No
                    failure = true;

                    // Append info
                    unreadableStatements.append(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "列表条目 $i{0}$b 无效;", obj.toString()));
                }
            }
            if (failure) {
                // Throw exception
                throw new IllegalArgumentException(
                        FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "无法读取附魔列表: ") + unreadableStatements.toString());
            }

            // No empty lists
            if (strlst.isEmpty()) {
                throw new IllegalArgumentException(
                        FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "升级操作列表为空"));
            }

            // Create ret
            EnchantUpgradeInfo eui = new EnchantUpgradeInfo();

            for (String str : strlst) {
                //UPGRD//MMOItems. Log("\u00a7e  --> \u00a77Entry \u00a76" + str);
                String[] split = str.split(" ");

                // At least two
                if (split.length >= 2) {

                    // Get
                    String enchStr = split[0];
                    String pmpStr = split[1];

                    // Adapt to PMP format
                    char c = pmpStr.charAt(0);
                    if (c == 's') {
                        pmpStr = pmpStr.substring(1);
                    } else if (c != '+' && c != '-' && c != 'n') {
                        pmpStr = '+' + pmpStr;
                    }

                    // Is it a valid plus minus percent?
                    FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
                    PlusMinusPercent pmpRead = PlusMinusPercent.getFromString(pmpStr, ffp);

                    Enchantment ench = null;
                    try {
                        ench = getEnchant(enchStr);
                    } catch (Exception ignored) {
                    }

                    // L
                    if (pmpRead == null) {
                        unreadableStatements.append(' ').append(ffp.getFeedbackOf(FriendlyFeedbackCategory.ERROR).get(0).forConsole(ffp.getPalette()));
                        failure = true;
                    }
                    if (ench == null) {
                        unreadableStatements.append(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "无效附魔 $i{0}$b", enchStr));
                        failure = true;
                    }

                    // Valid? add
                    if (pmpRead != null && ench != null) {
                        //UPGRD//MMOItems. Log("\u00a7a   s-> \u00a77Added");
                        eui.addEnchantmentOperation(ench, pmpRead);
                    }

                    // Not enough arguments to be read
                } else {

                    // Nope
                    failure = true;
                    unreadableStatements.append(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), " 列表条目 $i{0}$b 无效, 列表条目的格式为'esharpness +1$b'", str));
                }

            }
            if (failure) {
                // Throw exception
                throw new IllegalArgumentException(
                        FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "无法读取附魔列表: ") + unreadableStatements.toString());
            }

            // Success
            return eui;
        }

        public EnchantUpgradeInfo() {
        }

        /**
         * The operation every level will perform.
         *
         * @see PlusMinusPercent
         */
        @Nullable
        public PlusMinusPercent getPMP(@NotNull Enchantment ench) {
            return perEnchantmentOperations.get(ench);
        }

        /**
         * Includes an enchantment to be upgraded by this template.
         */
        public void addEnchantmentOperation(@NotNull Enchantment e, @NotNull PlusMinusPercent op) {
            perEnchantmentOperations.put(e, op);
        }

        /**
         * Which enchantments have operations defined here?
         */
        @NotNull
        public Set<Enchantment> getAffectedEnchantments() {
            return perEnchantmentOperations.keySet();
        }
    }
}
