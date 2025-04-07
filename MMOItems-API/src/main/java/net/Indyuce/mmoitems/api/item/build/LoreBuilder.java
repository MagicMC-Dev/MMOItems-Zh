package net.Indyuce.mmoitems.api.item.build;

import com.google.common.collect.Lists;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.util.annotation.BackwardsCompatibility;
import io.lumine.mythic.lib.util.formula.NumericalExpression;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ItemTier;
import net.Indyuce.mmoitems.tooltip.TooltipTexture;
import net.Indyuce.mmoitems.util.Buildable;
import net.Indyuce.mmoitems.util.MMOUtils;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * There are three types of lore placeholders.
 * - Classic placeholders are like #attack-damage# are called static placeholders.
 * - Special placeholders are {placeholder-name}, they can be used inside
 * the item lore, the one you get with {@link net.Indyuce.mmoitems.stat.Lore}
 * - Dynamic placeholders are %placeholder-name%, they are used by custom durability, consumable uses left, etc.
 *
 * @author Jules
 */
public class LoreBuilder extends Buildable<List<String>> {
    private final ItemStackBuilder parent;
    private final List<String> lore = new ArrayList<>();
    private final List<String> end = new ArrayList<>();
    private final Map<String, String> placeholders = new HashMap<>();

    public LoreBuilder(@NotNull ItemStackBuilder builder) {
        this.parent = builder;

        lore.addAll(MMOItems.plugin.getLore().getFormat(builder.getMMOItem()));

        registerPlaceholder("type", builder.getMMOItem().getType().getName());
        final ItemTier tier = builder.getMMOItem().getTier();
        registerPlaceholder("tier", tier != null ? tier.getName() : MMOItems.plugin.getLanguage().defaultTierName);
    }

    /**
     * Inserts a specific line at a specific index in the item lore.
     * Used by custom enchantment plugins to add enchant display to item lore.
     *
     * @param index   Index of insertion
     * @param element String to insert
     */
    public void insert(int index, @NotNull String element) {
        lore.add(index, element);
    }

    /**
     * Inserts specific lines at a specific index in the item lore.
     * Used by custom enchantment plugins to add enchant display to item lore.
     *
     * @param index    Index of insertion
     * @param elements Strings to insert
     */
    public void insert(int index, @NotNull Collection<String> elements) {
        lore.addAll(index, elements);
    }

    /**
     * Inserts a list of strings in the item lore. The lines are added only if a
     * line #item-stat-id# can be found in the lore format.
     *
     * @param path The path of the stat, used to locate where to insert the stat
     *             in the lore
     * @param add  The lines you want to add
     */
    public void insert(String path, String... add) {
        int index = lore.indexOf("#" + path + "#");
        if (index < 0) return;

        for (int j = 0; j < add.length; j++)
            lore.add(index + 1, add[add.length - j - 1]);
        lore.remove(index);
    }

    /**
     * Inserts a list of strings in the item lore. The lines are added only if a
     * line #item-stat-id# can be found in the lore format.
     *
     * @param path The path of the stat, used to locate where to insert the stat
     *             in the lore
     * @param list The lines you want to add
     */
    public void insert(@NotNull String path, @NotNull List<String> list) {
        int index = lore.indexOf("#" + path + "#");
        if (index < 0) return;

        Lists.reverse(list).forEach(string -> lore.add(index + 1, string));
        lore.remove(index);
    }

    /**
     * Registers a placeholder. All placeholders registered will be parsed when
     * using applyLorePlaceholders(String)
     *
     * @param path  The placeholder path (CASE SENSITIVE)
     * @param value The placeholder value which is instantly saved as a string
     *              when registered
     */
    public void registerPlaceholder(@NotNull String path, @Nullable Object value) {
        placeholders.put(path, String.valueOf(value));
    }

    /**
     * Parses a string with registered special placeholders
     *
     * @param str String with {..} unformatted placeholders
     * @return Same string with replaced placeholders. Placeholders which
     *         couldn't be found are marked with PHE which means
     *         PlaceHolderError
     */
    @NotNull
    public String applySpecialPlaceholders(String str) {

        int min = str.indexOf("{");
        while (min >= 0) {
            int max = str.indexOf("}", min);
            if (max < 0) break;

            // Compute and apply placeholder
            final String placeholder = str.substring(min + 1, max);
            final String value = placeholders.get(placeholder);
            if (value != null) {
                str = str.substring(0, min) + value + str.substring(max + 1);
                max += value.length() - placeholder.length() - 2;
            }

            // Goto next placeholder
            min = str.indexOf("{", max + 1);
        }

        return str;
    }

    /**
     * Adds a line of lore at the end of it
     *
     * @param str String to insert at the end
     */
    @Deprecated
    public void end(@NotNull String str) {
        end.add(str);
    }

    private static final String LINE_PREFIX = ChatColor.WHITE.toString();

    /**
     * TODO improve on complexity by precompiling the lore format, which will allow
     * TODO for better lore formatting in the future "en passant". This is O(n^2) and
     * TODO therefore extra shit because of the amount of List#remove calls being made.
     *
     * @return A built item lore. This method must be called after all lines
     *         have been inserted in the lore. It cleans all unused static placeholders
     *         as well as lore bars. The dynamic placeholders still remain however.
     */
    @Override
    protected List<String> whenBuilt() {
        // [BACKWARDS COMPATIBILITY] See Deprecated constructor. parent should not be null!
        TooltipTexture tooltip = parent != null ? parent.getTooltip() : null;

        /*
         * First, filtering iteration.
         *
         * Loops backwards to remove all unused bars in one iteration only.
         * The backwards loop allows to condense into one full iteration.
         */
        for (int j = 0; j < lore.size(); ) {
            final int n = lore.size() - j - 1;
            final String line = lore.get(n);

            // Remove unused static lore placeholders
            if (line.startsWith("#")) lore.remove(n);

                // Remove empty stat categories
            else if (line.startsWith("{bar}") && (j == 0 || getType(n + 1).isBar())) lore.remove(n);

            else j++;
        }

        // Apply extra lore lines from tooltip
        if (tooltip != null) {
            lore.add(tooltip.getBottom());
            if (tooltip.getLoreHeader() != null) lore.addAll(0, tooltip.getLoreHeader());
        }

        /*
         * Second and last, functional iteration.
         *
         * Steps In-order:
         * - Clear bad codes
         * - Apply placeholders and math
         * - Apply \n line breaks
         * - Apply tooltip middle/bar and suffix
         */
        final int linesIgnored = tooltip != null ? tooltip.getFirstIgnored() : 0;
        for (int j = 0; j < lore.size(); ) {
            String currentLine = lore.get(j);

            // Replace bar prefixes
            final LineType lineType = getType(j, currentLine);
            if (lineType.isNormalBar()) currentLine = currentLine.substring(5);
            if (lineType.isSuperBar()) currentLine = currentLine.substring(6);

            currentLine = MythicLib.plugin.getPlaceholderParser().parse(null, currentLine); // Apply PAPI placeholders
            currentLine = applySpecialPlaceholders(currentLine); // Apply internal placeholders

            // [BACKWARDS COMPATIBILITY] PAPI math expansion is now recommended
            final String match = MMOUtils.substringBetween(currentLine, "MATH%", "%");
            if (match != null) currentLine = currentLine.replaceFirst("MATH\\%[^%]*\\%", evaluateMathFormula(match));

            // Need to break down the line into multiple
            final boolean skipTooltipTexture = j < linesIgnored;
            final String[] split = currentLine.split("\n", -1);
            if (split.length > 1) {
                for (int k = split.length - 1; k >= 0; k -= 1) {
                    String subline = split[k];
                    if (tooltip != null && !lineType.isBottom())
                        subline = tooltip.bakeLoreLine(j, lineType, subline, skipTooltipTexture, k != split.length - 1);
                    lore.add(j, LINE_PREFIX + subline);
                }
                lore.remove(j + split.length); // Remove the old element
                j += split.length; // Increment by the right amount
            }

            // Simple line
            else {
                if (tooltip != null && !lineType.isBottom())
                    currentLine = tooltip.bakeLoreLine(j, lineType, currentLine, skipTooltipTexture, false);
                lore.set(j++, LINE_PREFIX + currentLine);
            }
        }

        if (tooltip != null && tooltip.debug) lore.add(0, LINE_PREFIX + "| <= Vanilla Text Aligns Here");

        lore.addAll(end);
        return lore;
    }

    @NotNull
    private LineType getType(int index) {
        return getType(index, lore.get(index));
    }

    /**
     * @param lineIndex   Current line counter
     * @param lineContent Current line
     * @return Type of current line lore.
     */
    @NotNull
    private LineType getType(int lineIndex, String lineContent) {
        if (lineIndex == lore.size() - 1) {
            if (lineContent.startsWith("{bar}")) return LineType.BOTTOM_BAR;
            if (lineContent.startsWith("{sbar}")) return LineType.BOTTOM_SUPERBAR;
            return LineType.BOTTOM;
        }
        if (lineContent.startsWith("{bar}")) return LineType.BAR;
        if (lineContent.startsWith("{sbar}")) return LineType.SUPERBAR;
        return LineType.MIDDLE;
    }

    public static enum LineType {
        MIDDLE, BAR, SUPERBAR, BOTTOM, BOTTOM_BAR, BOTTOM_SUPERBAR;

        public boolean isBar() {
            return isNormalBar() || isSuperBar();
        }

        public boolean isBottom() {
            return this == BOTTOM || this == BOTTOM_BAR || this == BOTTOM_SUPERBAR;
        }

        public boolean isSuperBar() {
            return this == SUPERBAR || this == BOTTOM_SUPERBAR;
        }

        public boolean isNormalBar() {
            return this == BAR || this == BOTTOM_BAR;
        }
    }

    @NotNull
    public List<String> getLore() {
        return lore;
    }

    public void setLore(List<String> lore) {
        this.lore.clear();
        this.lore.addAll(lore);
    }

    /**
     * Backwards compatibility. Java interpreter cannot find a method
     * if the method has the same name but a different location (the
     * #build method was moved to a parent class and no longer has the
     * same location)
     */
    @NotNull
    @Override
    public List<String> build() {
        return super.build();
    }

    //region Deprecated

    @Deprecated
    public LoreBuilder(@NotNull Collection<String> format) {
        lore.addAll(format);
        parent = null;
    }

    @Deprecated
    @BackwardsCompatibility(version = "unspecified")
    private String evaluateMathFormula(String formula) {
        try {
            return String.valueOf(NumericalExpression.eval(formula));
        } catch (Throwable throwable) {
            return "<ParsingError>";
        }
    }

    @Deprecated
    public boolean hasTooltip() {
        return parent != null && parent.getTooltip() != null;
    }

    @Deprecated
    public TooltipTexture getTooltip() {
        return parent != null ? parent.getTooltip() : null;
    }

    //endregion
}
