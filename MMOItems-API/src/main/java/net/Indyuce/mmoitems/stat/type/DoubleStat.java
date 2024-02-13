package net.Indyuce.mmoitems.stat.type;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.PlusMinusPercent;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import io.lumine.mythic.lib.manager.StatManager;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.util.MMOUtils;
import net.Indyuce.mmoitems.api.UpgradeTemplate;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.data.type.StatData;
import net.Indyuce.mmoitems.stat.data.type.UpgradeInfo;
import org.apache.commons.lang.Validate;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;


public class DoubleStat extends ItemStat<NumericStatFormula, DoubleData> implements Upgradable, Previewable<NumericStatFormula, DoubleData> {
    private final boolean moreIsBetter;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.####");

    public DoubleStat(String id, Material mat, String name, String[] lore) {
        this(id, mat, name, lore, new String[]{"!miscellaneous", "!block", "all"}, true);
    }

    public DoubleStat(String id, Material mat, String name, String[] lore, String[] types, Material... materials) {
        this(id, mat, name, lore, types, true, materials);
    }

    public DoubleStat(String id, Material mat, String name, String[] lore, String[] types, boolean moreIsBetter, Material... materials) {
        super(id, mat, name, lore, types, materials);

        this.moreIsBetter = moreIsBetter;
    }

    /**
     * @return If this stat supports negatives stat values
     */
    public boolean handleNegativeStats() {
        return true;
    }

    /**
     * @return For example knockback resistance, 0.01 = 1% so multiplies by 100 when displaying.
     */
    public double multiplyWhenDisplaying() {
        return 1;
    }

    /**
     * Usually, a greater magnitude of stat benefits the player (more health, more attack damage).
     * <p>However, its not impossible for a stat to be evil instead, who knows?
     */
    public boolean moreIsBetter() {
        return moreIsBetter;
    }

    @Override
    public NumericStatFormula whenInitialized(Object object) {

        if (object instanceof Number)
            return new NumericStatFormula(Double.parseDouble(object.toString()), 0, 0, 0);

        if (object instanceof ConfigurationSection)
            return new NumericStatFormula(object);

        throw new IllegalArgumentException("必须指定一个数字或配置部分");
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {

        // Get Value
        double value = data.getValue();

        // Cancel if it its NEGATIVE and this doesn't support negative stats.
        if (value < 0 && !handleNegativeStats()) {
            return;
        }

        // Identify the upgrade amount
        double upgradeShift = 0;

        // Displaying upgrades?
        if (UpgradeTemplate.isDisplayingUpgrades() && item.getMMOItem().getUpgradeLevel() != 0) {

            // Get stat history
            StatHistory hist = item.getMMOItem().getStatHistory(this);
            if (hist != null) {

                // Get as if it had never been upgraded
                //HSY//MMOItems.log(" \u00a73-\u00a7a- \u00a77Stat Change Display Recalculation \u00a73-\u00a7a-\u00a73-");
                DoubleData uData = (DoubleData) hist.recalculateUnupgraded();

                // Calculate Difference
                upgradeShift = value - uData.getValue();
            }
        }

        // Display in lore
        if (value != 0 || upgradeShift != 0) {
            String loreInsert = formatPath(getId(), getGeneralStatFormat(), moreIsBetter, value * multiplyWhenDisplaying());
            if (upgradeShift != 0)
                loreInsert += UpgradeTemplate.getUpgradeChangeSuffix(plus(true, upgradeShift * multiplyWhenDisplaying()) + (MythicLib.plugin.getMMOConfig().decimals.format(upgradeShift * multiplyWhenDisplaying())), !isGood(upgradeShift * multiplyWhenDisplaying()));
            item.getLore().insert(getPath(), loreInsert);
        }

        /*
         * Add NBT Data if it is not equal to ZERO, in which case it will just get removed.
         *
         * It is important that the tags are not excluded in getAppliedNBT() because the StatHistory does
         * need that blanc tag information to remember when an Item did not initially have any of a stat.
         */
        if (data.getValue() != 0) {
            item.addItemTag(getAppliedNBT(data));
        }
    }

    @Deprecated
    public static String formatPath(@NotNull String format, boolean moreIsBetter, double value) {
        return formatPath("ATTACK_DAMAGE", format, moreIsBetter, value);
    }

    @Deprecated
    public static String formatPath(@NotNull String format, boolean moreIsBetter, double min, double max) {
        return formatPath("ATTACK_DAMAGE", format, moreIsBetter, min, max);
    }

    @NotNull
    public static String formatPath(@NotNull String stat, @NotNull String format, boolean moreIsBetter, double value) {
        return formatPath(stat, format, moreIsBetter, true, value);
    }

    @NotNull
    public static String formatPath(@NotNull String stat, @NotNull String format, boolean moreIsBetter, boolean displayPlus, double value) {
        final String valueFormatted = StatManager.format(stat, value);
        final String colorPrefix = getColorPrefix(value < 0 && moreIsBetter);
        return format
                .replace("{value}", colorPrefix + valueFormatted) // Replace loose pounds with the value
                .replace("<plus>", colorPrefix + plus(displayPlus, value)); // Replace loose <plus>es
    }

    @NotNull
    public static String formatPath(@NotNull String stat, @NotNull String format, boolean moreIsBetter, double min, double max) {
        return formatPath(stat, format, moreIsBetter, true, min, max);
    }

    @NotNull
    public static String formatPath(@NotNull String stat, @NotNull String format, boolean moreIsBetter, boolean displayPlus, double min, double max) {

        // For stat preview
        if (SilentNumbers.round(min, 2) == SilentNumbers.round(max, 2))
            return formatPath(stat, format, moreIsBetter, displayPlus, min);

        final String minFormatted = plus(displayPlus, min) + StatManager.format(stat, min), maxFormatted = (displayPlus && min < 0 && max > 0 ? "+" : "") + StatManager.format(stat, max);
        final String minPrefix = getColorPrefix(min < 0 && moreIsBetter), maxPrefix = getColorPrefix(max < 0 && moreIsBetter);
        return format
                .replace("<plus>", "") // Override plus
                .replace("{value}",
                        minPrefix + minFormatted
                                + MMOItems.plugin.getConfig().getString("stats-displaying.range-dash", "⎓") +
                                maxPrefix + maxFormatted);
    }

    @NotNull
    private static String getColorPrefix(boolean isNegative) {

        // Get the base
        return MMOItems.plugin.getConfig().getString("stats-displaying.color-" + (isNegative ? "negative" : "positive"), "");
    }

    @NotNull
    private static String plus(boolean displayPlus, double amount) {
        return displayPlus && amount > 0 ? "+" : "";
    }

    @Override
    public void whenPreviewed(@NotNull ItemStackBuilder item, @NotNull DoubleData currentData, @NotNull NumericStatFormula templateData) throws IllegalArgumentException {
        Validate.isTrue(currentData instanceof DoubleData, "当前数据不是双精度数据");
        Validate.isTrue(templateData instanceof NumericStatFormula, "模板数据不是数字统计公式");

        // Get Value
        //SPRD//MMOItems.log("\u00a7c༺\u00a77 Calulating deviations of \u00a7b" + item.getMMOItem().getType().toString() + " " + item.getMMOItem().getId() + "\u00a77's \u00a7e" + getId());
        double techMinimum = templateData.calculate(0, NumericStatFormula.FormulaInputType.LOWER_BOUND);
        double techMaximum = templateData.calculate(0, NumericStatFormula.FormulaInputType.UPPER_BOUND);

        // Cancel if it its NEGATIVE and this doesn't support negative stats.
        if (techMaximum < 0 && !handleNegativeStats()) {
            return;
        }
        if (techMinimum < 0 && !handleNegativeStats()) {
            techMinimum = 0;
        }
        // Add NBT Path
        item.addItemTag(getAppliedNBT(currentData));

        // Display if not ZERO
        if (techMinimum != 0 || techMaximum != 0) {
            final String builtRange = formatPath(getId(), getGeneralStatFormat(), moreIsBetter(), techMinimum * multiplyWhenDisplaying(), techMaximum * multiplyWhenDisplaying());
            item.getLore().insert(getPath(), builtRange);
        }
    }

    /**
     * Usually, a greater magnitude of stat benefits the player (more health, more attack damage).
     * <p>However, its not impossible for a stat to be evil instead, who knows?
     * <p></p>
     * This will return true if:
     * <p> > The amount is positive, and more benefits the player
     * </p> > The amount is negative, and more hurts the player
     */
    public boolean isGood(double amount) {
        return moreIsBetter() ? amount >= 0 : amount <= 0;
    }

    @Override
    @NotNull
    public ArrayList<ItemTag> getAppliedNBT(@NotNull DoubleData data) {

        // Create Fresh
        ArrayList<ItemTag> ret = new ArrayList<>();

        // Add sole tag
        ret.add(new ItemTag(getNBTPath(), data.getValue()));

        // Return thay
        return ret;
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {

        // Get tags
        ArrayList<ItemTag> relevantTags = new ArrayList<>();

        // Add sole tag
        if (mmoitem.getNBT().hasTag(getNBTPath()))
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.DOUBLE));

        // Use that
        DoubleData bakedData = getLoadedNBT(relevantTags);

        // Valid?
        if (bakedData != null) {

            // Set
            mmoitem.setData(this, bakedData);
        }
    }

    @Override
    @Nullable
    public DoubleData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {

        // You got a double righ
        ItemTag tg = ItemTag.getTagAtPath(getNBTPath(), storedTags);

        // Found righ
        if (tg != null) {

            // Thats it
            return new DoubleData(SilentNumbers.round((Double) tg.getValue(), 4));
        }

        // Fail
        return null;
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        if (event.getAction() == InventoryAction.PICKUP_HALF) {
            inv.getEditedSection().set(getPath(), null);
            inv.registerTemplateEdition();
            inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + "成功删除 " + getName() + ChatColor.GRAY + ".");
            return;
        }
        new StatEdition(inv, this).enable("在聊天中输入您想要的数值",
                "第二种格式: {基础} {缩放值} {点差} {最大点差}", "第三种格式: {最小值} -> {最大值}");
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        final NumericStatFormula formula = new NumericStatFormula(message);
        formula.fillConfigurationSection(inv.getEditedSection(), getPath(), NumericStatFormula.FormulaSaveOption.DELETE_IF_ZERO);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(MMOItems.plugin.getPrefix() + getName() + "成功更改为 { "
                + formula.getBase() + " - " + formula.getScale() + " - " + formula.getSpread() + " - " + formula.getMaxSpread() + " - ("
                + formula.getMin() + " -> " + formula.getMax() + ") }");
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<NumericStatFormula> statData) {
        if (statData.isPresent()) {
            NumericStatFormula data = statData.get();
            if (data.isUniform()) {
                lore.add(ChatColor.GRAY + "更改: " + ChatColor.GREEN + DECIMAL_FORMAT.format(data.getMin()) + ChatColor.GRAY + " -> " + ChatColor.GREEN + DECIMAL_FORMAT.format(data.getMax()));
            } else {
                lore.add(ChatColor.GRAY + "基础值: " + ChatColor.GREEN + DECIMAL_FORMAT.format(data.getBase())
                        + (data.getScale() != 0 ? ChatColor.GRAY + " (+" + ChatColor.GREEN + DECIMAL_FORMAT.format(data.getScale()) + ChatColor.GRAY + "/Lvl)" : ""));
                if (data.getSpread() > 0)
                    lore.add(ChatColor.GRAY + "速度: " + ChatColor.GREEN + DECIMAL_FORMAT.format(data.getSpread() * 100) + "%" + ChatColor.GRAY + " (Max: "
                            + ChatColor.GREEN + DECIMAL_FORMAT.format(data.getMaxSpread() * 100) + "%" + ChatColor.GRAY + ")");
                if (data.hasMin())
                    lore.add(ChatColor.GRAY + "最小: " + ChatColor.GREEN + DECIMAL_FORMAT.format(data.getMin()));
                if (data.hasMax())
                    lore.add(ChatColor.GRAY + "最大: " + ChatColor.GREEN + DECIMAL_FORMAT.format(data.getMax()));
            }
        } else
            lore.add(ChatColor.GRAY + "当前值: " + ChatColor.GREEN + "---");

        lore.add("");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 左键单击可更改此值");
        lore.add(ChatColor.YELLOW + AltChar.listDash + " 右键单击可删除该值");
    }

    @Override
    @NotNull
    public DoubleData getClearStatData() {
        return new DoubleData(0D);
    }

    @NotNull
    @Override
    public UpgradeInfo loadUpgradeInfo(@Nullable Object obj) throws IllegalArgumentException {

        // Return result of thay
        return DoubleUpgradeInfo.GetFrom(obj);
    }

    @NotNull
    @Override
    public StatData apply(@NotNull StatData original, @NotNull UpgradeInfo info, int level) {

        // Must be DoubleData
        int i = level;
        if (original instanceof DoubleData && info instanceof DoubleUpgradeInfo) {

            // Get value
            double value = ((DoubleData) original).getValue();

            // If leveling up
            if (i > 0) {

                // While still positive
                while (i > 0) {

                    // Apply PMP Operation Positively
                    value = ((DoubleUpgradeInfo) info).getPMP().apply(value);

                    // Decrease
                    i--;
                }

                // Degrading the item
            } else if (i < 0) {

                // While still negative
                while (i < 0) {

                    // Apply PMP Operation Reversibly
                    value = ((DoubleUpgradeInfo) info).getPMP().reverse(value);

                    // Decrease
                    i++;
                }
            }

            // Update
            ((DoubleData) original).setValue(value);
        }

        // Upgraded
        return original;
    }

    public static class DoubleUpgradeInfo implements UpgradeInfo {
        @NotNull
        PlusMinusPercent pmp;

        /**
         * Generate a <code>DoubleUpgradeInfo</code> from this <code><b>String</b></code>
         * that represents a {@link PlusMinusPercent}.
         * <p></p>
         * To keep older MMOItems versions working the same way, instead of having no prefix
         * to use the <i>set</i> function of the PMP, one must use an <b><code>s</code></b> prefix.
         *
         * @param obj A <code><u>String</u></code> that encodes for a PMP.
         * @throws IllegalArgumentException If any part of the operation goes wrong (including reading the PMP).
         */
        @NotNull
        public static DoubleUpgradeInfo GetFrom(@Nullable Object obj) throws IllegalArgumentException {

            // Shall not be null
            Validate.notNull(obj, FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "升级操作不能为空"));

            // Does the string exist?
            String str = obj.toString();
            if (str.isEmpty()) {
                throw new IllegalArgumentException(
                        FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "升级操作为空"));
            }

            // Adapt to PMP format
            char c = str.charAt(0);
            if (c == 's') {
                str = str.substring(1);
            } else if (c != '+' && c != '-' && c != 'n') {
                str = '+' + str;
            }

            // Is it a valid plus minus percent?
            FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
            PlusMinusPercent pmpRead = PlusMinusPercent.getFromString(str, ffp);
            if (pmpRead == null) {
                throw new IllegalArgumentException(
                        ffp.getFeedbackOf(FriendlyFeedbackCategory.ERROR).get(0).forConsole(ffp.getPalette()));
            }

            // Success
            return new DoubleUpgradeInfo(pmpRead);
        }

        public DoubleUpgradeInfo(@NotNull PlusMinusPercent pmp) {
            this.pmp = pmp;
        }

        /**
         * The operation every level will perform.
         *
         * @see PlusMinusPercent
         */
        @NotNull
        public PlusMinusPercent getPMP() {
            return pmp;
        }
    }
}
