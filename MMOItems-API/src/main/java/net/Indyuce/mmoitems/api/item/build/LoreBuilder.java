package net.Indyuce.mmoitems.api.item.build;

import com.google.common.collect.Lists;
import io.lumine.mythic.lib.MythicLib;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.ConfigFile;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * There are three types of lore placeholders.
 * - Classic placeholders are like #attack-damage# are called static placeholders.
 * - Special placeholders are {placeholder-name}, they can be used inside of
 * the item lore, the one you get with {@link net.Indyuce.mmoitems.stat.Lore}
 * - Dynamic placeholders are %placeholder-name%, they
 * are used by custom durability, consumable uses left, etc.
 *
 * @author indyuce
 */
public class LoreBuilder {
    private final List<String> lore = new ArrayList<>();
    private final List<String> end = new ArrayList<>();
    private final TooltipTexture tooltip = null;
    private final Map<String, String> placeholders = new HashMap<>();

    private boolean built;

    @Deprecated
    public LoreBuilder(@NotNull Collection<String> format) {
        lore.addAll(format);
    }
/*
    @Deprecated
    public static final TooltipTexture TEST = new TooltipTexture(new ConfigFile("tooltips").getConfig().getConfigurationSection("test"));*/

    public LoreBuilder(@NotNull MMOItem mmoitem) {
        lore.addAll(MMOItems.plugin.getFormats().getFormat(mmoitem));

        // TODO load tooltip
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
     * Inserts a list of strings in the item lore. The lines are added only if a
     * line #item-stat-id# can be found in the lore format.
     *
     * @param path The path of the stat, used to locate where to insert the stat
     *             in the lore
     * @param add  The lines you want to add
     */
    public void insert(String path, String... add) {
        int index = lore.indexOf("#" + path + "#");
        if (index < 0)
            return;

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
        if (index < 0)
            return;

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

        while (str.contains("{") && str.substring(str.indexOf("{")).contains("}")) {
            String holder = str.substring(str.indexOf("{") + 1, str.indexOf("}"));
            str = str.replace("{" + holder + "}", placeholders.getOrDefault(holder, "PHE"));
        }

        return str;
    }

    /**
     * Adds a line of lore at the end of it
     *
     * @param str String to insert at the end
     */
    public void end(@NotNull String str) {
        end.add(str);
    }

    /**
     * @return A built item lore. This method must be called after all lines
     *         have been inserted in the lore. It cleans all unused static placeholders
     *         as well as lore bars. The dynamic placeholders still remain however.
     */
    @NotNull
    public List<String> build() {
        Validate.isTrue(!built, "Lore is already built");
        built = true;

        /*
         * First, filtering iteration.
         *
         * Loops backwards to remove all unused bars in one iteration only.
         * The backwards loop allows to condense into one full iteration.
         */
        for (int j = 0; j < lore.size(); ) {
            int n = lore.size() - j - 1;
            String line = lore.get(n);

            // Remove unused static lore placeholders
            if (line.startsWith("#"))
                lore.remove(n);

                // Remove empty stat categories
            else if (line.startsWith("{bar}") && (n == lore.size() - 1 || isBar(lore.get(n + 1))))
                lore.remove(n);

            else
                j++;
        }

        /*
         * Second and last, functional step.
         *
         * Steps In-order:
         * - Clear bad codes
         * - Apply placeholders and math
         * - Apply \n line breaks
         * - Apply tooltip middle/bar and suffix
         */
        final String effectiveSuffix = tooltip != null ? tooltip.getSuffix() : "";
        for (int j = 0; j < lore.size(); ) {
            String currentLine = lore.get(j);

            // Replace bar prefixes
            final boolean bar = currentLine.startsWith("{bar}"), superbar = currentLine.startsWith("{sbar}");
            if (bar) currentLine = currentLine.substring(5);
            if (superbar) currentLine = currentLine.substring(6);

            // Apply tooltip prefixes if necessary
            if (tooltip != null)
                currentLine = (bar || superbar ? tooltip.getBar() : tooltip.getMiddle()) + currentLine;

            // Deprecated math. PAPI math expansion is now recommended
            final String match = StringUtils.substringBetween(currentLine, "MATH%", "%");
            if (match != null) currentLine = currentLine.replaceFirst("MATH\\%[^%]*\\%", evaluate(match));

            // Apply PAPI placeholders
            currentLine = MythicLib.plugin.getPlaceholderParser().parse(null, currentLine);

            // Need to break down the line into multiple
            if (currentLine.contains("\\n")) {
                String[] split = currentLine.split("\\\\n");
                for (int k = split.length - 1; k >= 0; k -= 1)
                    lore.add(j, split[k] + effectiveSuffix);

                // Remove the old element
                lore.remove(j + split.length);

                // Increment by the right amount
                j += split.length;

            } else

                // Simple line
                lore.set(j++, currentLine + effectiveSuffix);
        }

        // Apply tooltip bottom
        if (tooltip != null) {
            lore.add(tooltip.getBottom() + effectiveSuffix);

            // Apply tooltip lore header
            if (tooltip.getLoreHeader() != null) lore.addAll(0, tooltip.getLoreHeader());
        }

        lore.addAll(end);
        return lore;
    }

    @Deprecated
    private String evaluate(String formula) {
        try {
            return MythicLib.plugin.getMMOConfig().decimals.format((double) MythicLib.plugin.getFormulaParser().eval(formula));
        } catch (Throwable throwable) {
            return "<ParsingError>";
        }
    }

    private boolean isBar(String str) {
        return str.startsWith("{bar}") || str.startsWith("{sbar}");
    }

    @NotNull
    public List<String> getLore() {
        return lore;
    }

    public boolean hasTooltip() {
        return tooltip != null;
    }

    @NotNull
    public TooltipTexture getTooltip() {
        return Objects.requireNonNull(tooltip);
    }

    public void setLore(List<String> lore) {
        this.lore.clear();
        this.lore.addAll(lore);
    }
}
