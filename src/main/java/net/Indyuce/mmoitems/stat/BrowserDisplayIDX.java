package net.Indyuce.mmoitems.stat;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.util.NumericStatFormula;
import net.Indyuce.mmoitems.stat.data.DoubleData;
import net.Indyuce.mmoitems.stat.type.DoubleStat;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BrowserDisplayIDX extends DoubleStat {

    public BrowserDisplayIDX() { super("BROWSER_IDX", Material.GHAST_TEAR, "Browser Index", new String[] {"Used to display similar items together,", "neatly in the GUI \u00a7a/mmoitems browse", "", "Items with the same index are grouped."}, new String[]{"all"}); }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull DoubleData data) {
        // Does not participate in actual items
    }

    @Override public void whenLoaded(@NotNull ReadMMOItem mmoitem) { }

    /**
     * They will be ordered.
     *
     * @return The MMOItem Templates separated by Index. Those with no index
     *         will be linked to the null index.
     */
    @NotNull public static HashMap<Double, ArrayList<MMOItemTemplate>> select(@NotNull Collection<MMOItemTemplate> templates) {
        HashMap<Double, ArrayList<MMOItemTemplate>> ret = new HashMap<>();

        // Go through them all
        for (MMOItemTemplate template : templates) {
            if (template == null) { continue; }

            Double armorIDX = null;
            if (template.getType().getAvailableStats().contains(ItemStats.BROWSER_DISPLAY_IDX)) {
                NumericStatFormula indexData = (NumericStatFormula) template.getBaseItemData().get(ItemStats.BROWSER_DISPLAY_IDX);

                // Get value if it existed
                if (indexData != null && indexData.getBase() != 0) { armorIDX = indexData.getBase(); }
            }

            // Get that map
            ArrayList<MMOItemTemplate> perIndexTemplates = ret.get(armorIDX);
            if (perIndexTemplates == null) { perIndexTemplates = new ArrayList<>(); }

            // Include template
            perIndexTemplates.add(template);
            ret.put(armorIDX, perIndexTemplates);
        }

        return ret;
    }

    /**
     * @param i Index you search
     *
     * @param templates Templates from which to gather
     *
     * @return The Ith Template of this Array.
     */
    @Nullable public static MMOItemTemplate getAt(int i, @NotNull HashMap<Double, ArrayList<MMOItemTemplate>> templates) {
        Map.Entry<Double, ArrayList<MMOItemTemplate>> nullEntry = null;

        // Iterate every entry
        for (Map.Entry<Double, ArrayList<MMOItemTemplate>> entry : templates.entrySet()) {

            // Null entry are always displayed at the end, reference and skip.
            if (entry.getKey() == null) { nullEntry = entry; continue; }

            // Identify list, add null entries until it has a size multiple of four.
            @NotNull ArrayList<MMOItemTemplate> list = entry.getValue();
            while (list.size() % 4 != 0) { list.add(null); }

            /*
             * Go through each entry until i equals zero
             */
            for (MMOItemTemplate observed : list) {

                // Yes
                if (i == 0) { return observed; }
                i--;
            }
        }

        // No more
        if (nullEntry == null) { return null; }

        // Still standing...
        @NotNull ArrayList<MMOItemTemplate> list = nullEntry.getValue();

        /*
         * Go through each entry until i equals zero
         */
        for (MMOItemTemplate observed : list) {

            // Yes
            if (i == 0) { return observed; }
            i--;
        }

        // None found
        return null;
    }
}
