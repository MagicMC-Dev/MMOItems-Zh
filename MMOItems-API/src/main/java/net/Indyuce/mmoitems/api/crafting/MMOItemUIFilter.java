package net.Indyuce.mmoitems.api.crafting;

import io.lumine.mythic.lib.api.crafting.uifilters.UIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.UIFilterManager;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.ItemFactory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.QuickNumberRange;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.build.MMOItemBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.stat.data.UpgradeData;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * A filter to identify MMOItems :)
 *
 * @author Gunging
 */
public class MMOItemUIFilter implements UIFilter {
    @NotNull
    @Override
    public String getIdentifier() {
        return "m";
    }

    @Override
    public boolean matches(@NotNull ItemStack item, @NotNull String argument, @NotNull String data, @Nullable FriendlyFeedbackProvider ffp) {

        // To format
        argument = argument.replace(" ", "_").replace("-", "_").toUpperCase();
        data = data.replace(" ", "_").replace("-", "_").toUpperCase();

        // Check validity
        if (!isValid(argument, data, ffp)) { return false; }

        // Check counter matches
        if (cancelMatch(item, ffp)) { return false; }

        // Is this item a MMOItem?
        NBTItem asNBT = NBTItem.get(item);
        MMOItemTemplate mmo = MMOItems.plugin.getTemplates().getTemplate(asNBT);
        if (mmo == null) {

            // Notify
            FriendlyFeedbackProvider.log(ffp, FriendlyFeedbackCategory.FAILURE,
                    "Item $r{0}$b is $fnot a MMOItem$b. ", SilentNumbers.getItemName(item));

            // Fail
            return false;
        }

        // Strip data
        @NotNull String dataments = "";
        if (data.contains("{")) {

            // Just clip them out for now yea
            dataments = data.substring(data.indexOf('{') + 1);
            data = data.substring(0, data.indexOf('{'));
            if (dataments.endsWith("}")) { dataments = dataments.substring(0, dataments.length()-1); } }

        // All right get its Type and ID
        if (!mmo.getType().getId().equals(argument) || !mmo.getId().equals(data)) {

            // Notify
            FriendlyFeedbackProvider.log(ffp, FriendlyFeedbackCategory.FAILURE,
                    "MMOItem $r{0} {1}$b is not the expected $r{2} {3}$b. $fNo Match. ",
                    mmo.getType().getId(), mmo.getId(), argument, data);

            // Fail
            return false;
        }

        // Find upgrade?
        if (!dataments.isEmpty()) {
            VolatileMMOItem vmmo = new VolatileMMOItem(asNBT);

            // Get
            QuickNumberRange upgradeReq = SilentNumbers.rangeFromBracketsTab(dataments, "LEVEL");
            if (upgradeReq != null) {

                // Upgrade data?
                int identifiedLvl = vmmo.getUpgradeLevel();

                // Not matched?
                if (!upgradeReq.inRange(identifiedLvl)) {

                    // Notify
                    FriendlyFeedbackProvider.log(ffp, FriendlyFeedbackCategory.FAILURE,
                            "MMOItem $r{0} {1}$b is of level $u{2}$b though $r{3}$b was expected. $fNo Match. ",
                            mmo.getType().getId(), mmo.getId(), String.valueOf(identifiedLvl), upgradeReq.toString());

                    // Fail
                    return false;
                }
            }
        }

        // Notify
        FriendlyFeedbackProvider.log(ffp, FriendlyFeedbackCategory.SUCCESS,
                "Detected $r{0} {1} $sSuccessfully. ", mmo.getType().getId(), mmo.getId());

        return true;
    }

    @Override
    public boolean isValid(@NotNull String argument, @NotNull String data, @Nullable FriendlyFeedbackProvider ffp) {
        if (reg) { return true; }
        argument = argument.replace(" ", "_").replace("-", "_").toUpperCase();
        data = data.replace(" ", "_").replace("-", "_").toUpperCase();

        // Type exists?
        Type t = MMOItems.plugin.getTypes().get(argument);

        // Nope
        if (t == null) {

            // Error
            FriendlyFeedbackProvider.log(ffp, FriendlyFeedbackCategory.ERROR,
                    "$fInvalid MMOItem Type $r{0}$f. ", argument);
            
            return false;
        }

        // Parse data
        if (data.contains("{") && data.contains("}")) {

            // Just clip them out for now yea
            data = data.substring(0, data.indexOf('{')); }

        // Can find item?
        if (MMOItems.plugin.getMMOItem(t, data) == null) {


            // Error
            FriendlyFeedbackProvider.log(ffp, FriendlyFeedbackCategory.ERROR,
                    "$fInvalid MMOItem $r{0} {1}$f: $bNo such MMOItem for Type $e{0}$b. ", argument, data);

            return false;
        }

        // Error
        FriendlyFeedbackProvider.log(ffp, FriendlyFeedbackCategory.SUCCESS,
                "Valid MMOItem $r{0} {1}$b. $snice. ", argument, data);
        return true;
    }

    @NotNull
    @Override
    public ArrayList<String> tabCompleteArgument(@NotNull String argument) {

        // Filter from the available types
        return SilentNumbers.smartFilter(MMOItems.plugin.getTypes().getAllTypeNames(), argument, true);
    }

    @NotNull
    @Override
    public ArrayList<String> tabCompleteData(@NotNull String argument, @NotNull String data) {
        
        //Find type? 
        Type t = MMOItems.plugin.getTypes().get(argument);
        
        if (t != null) {

            // Strip data
            @NotNull String dataments = "", datamentsTab = "";
            if (data.contains("{")) {

                // Just clip them out for now yea
                dataments = data.substring(data.indexOf('{') + 1);
                data = data.substring(0, data.indexOf('{'));

                int datashort = 0;
                if (dataments.contains(",")) { datashort = dataments.lastIndexOf(',') + 1; }

                datamentsTab = dataments.substring(datashort);
                dataments = dataments.substring(0, datashort);
            }

            // Just filter among template names of this type
            ArrayList<String> suggestions = SilentNumbers.smartFilter(MMOItems.plugin.getTemplates().getTemplateNames(t), data, true);
            ArrayList<String> trueSuggestions = suggestions;

            // So, what things may be put in data?
            if (!datamentsTab.isEmpty()) {
                ArrayList<String> datamentsSug = new ArrayList<>();

                // All right, so right now
                if (datamentsTab.contains("=")) {

                    // All right grab
                    String datamentsUsed = datamentsTab.substring(0, datamentsTab.indexOf('='));
                    switch (datamentsUsed.toLowerCase()) {
                        case "level":
                            SilentNumbers.addAll(datamentsSug, "level=1..", "level=2..4", "level=..6");
                            break;
                        default:
                            datamentsSug.add(datamentsTab);
                            break;
                    }

                // No equals sign, suggest dataments
                } else {

                    // Suggest that
                    datamentsSug = SilentNumbers.smartFilter(getValidDataments(), datamentsTab, true);
                }

                // Compile
                for (String sug : suggestions) {
                    for (String comp : datamentsSug) {
                        // All the suggestions that could complete this..
                        trueSuggestions.add(sug + "{" + dataments + comp); } }
            }

            // That's it
            return trueSuggestions;

        } else {

            // Typo in the Type
            return SilentNumbers.toArrayList("Type_not_found,_check_your_spelling");
        }
    }

    ArrayList<String> validDataments;
    @NotNull public ArrayList<String> getValidDataments() { if (validDataments != null) { return validDataments; } validDataments = SilentNumbers.toArrayList("level"); return validDataments; }

    @Override
    public boolean fullyDefinesItem() { return true; }

    @Nullable
    @Override
    public ItemStack getItemStack(@NotNull String argument, @NotNull String data, @Nullable FriendlyFeedbackProvider ffp) {
        if (!isValid(argument, data, ffp)) { return null; }
        argument = argument.replace(" ", "_").replace("-", "_").toUpperCase();
        data = data.replace(" ", "_").replace("-", "_").toUpperCase();
        return MMOItems.plugin.getItem(argument, data);
    }

    @NotNull
    @Override
    public ItemStack getDisplayStack(@NotNull String argument, @NotNull String data, @Nullable FriendlyFeedbackProvider ffp) {
        if (!isValid(argument, data, ffp)) { return ItemFactory.of(Material.STRUCTURE_VOID).name("\u00a7cInvalid MMOItem \u00a7e" + argument + " " + data).build(); }
        argument = argument.replace(" ", "_").replace("-", "_").toUpperCase();

        // Strip data
        @NotNull String dataments = "";
        if (data.contains("{")) {

            // Just clip them out for now yea
            dataments = data.substring(data.indexOf('{') + 1);
            data = data.substring(0, data.indexOf('{'));
            if (dataments.endsWith("}")) { dataments = dataments.substring(0, dataments.length()-1); } }

        data = data.replace(" ", "_").replace("-", "_").toUpperCase();
        MMOItem m = new MMOItemBuilder(MMOItems.plugin.getTemplates().getTemplate(MMOItems.plugin.getTypes().get(argument), data), 0, null, true).build();

        // Find upgrade?
        if (!dataments.isEmpty()) {
            //UPGR//MMOItems.log(" \u00a73>\u00a7a> \u00a77Dataments of \u00a7e" + argument + " " + data + "\u00a77: \u00a73 " + dataments);

            // Requires upgrade template :flushed:
            //noinspection ConstantConditions
            if (m.hasUpgradeTemplate()) {
                //UPGR//MMOItems.log(" \u00a73>\u00a7a> \u00a77Seeking upgrade\u00a7b " + SilentNumbers.valueFromBracketsTab(dataments, "level"));

                // Get
                QuickNumberRange upgradeReq = SilentNumbers.rangeFromBracketsTab(dataments, "level");
                if (upgradeReq != null) {
                    //UPGR//MMOItems.log(" \u00a73>\u00a7a> \u00a77Found upgrade\u00a76 " + upgradeReq);
                    UpgradeData ud = ((UpgradeData) m.getData(ItemStats.UPGRADE)).clone();
                    ud.setLevel(SilentNumbers.floor(upgradeReq.getAsDouble(0)));
                    m.setData(ItemStats.UPGRADE, ud);
                }
            }
        }

        // Build display NBT and roll
        return m.newBuilder().buildNBT(true).toItem();
    }

    @NotNull
    @Override
    public ArrayList<String> getDescription(@NotNull String argument, @NotNull String data) {

        // Check validity
        if (!isValid(argument, data, null)) { return SilentNumbers.toArrayList("This MMOItem is $finvalid$b."); }
        argument = argument.replace(" ", "_").replace("-", "_").toUpperCase();
        data = data.replace(" ", "_").replace("-", "_").toUpperCase();
        return SilentNumbers.toArrayList(SilentNumbers.getItemName(MMOItems.plugin.getItem(argument, data)));
    }

    @Override
    public boolean determinateGeneration() { return false; }

    @Override
    public boolean partialDeterminateGeneration(@NotNull String argument, @NotNull String data) {

        // Guaranteed to not work 100% of the time yes
        return !isValid(argument, data, null);

        //todo Detect if this MMOItem template has no random Stat Data,
        //     nor the 'Unstackable' Stat, to really optimize when
        //     crafting many items.
    }

    @NotNull
    @Override
    public String getSourcePlugin() {
        return "MMOItems";
    }

    @NotNull
    @Override
    public String getFilterName() {
        return "MMOItem";
    }

    @NotNull
    @Override
    public String exampleArgument() {
        return "CONSUMABLE";
    }

    @NotNull
    @Override
    public String exampleData() {
        return "MANGO";
    }

    /**
     * Registers this filter onto the manager.
     */
    public static void register() {
        global = new MMOItemUIFilter();
        UIFilterManager.registerUIFilter(global);
        VanillaMMOItemCountermatch.enable();
        reg = false; }

    private static boolean reg = true;

    /**
     * @return The general instance of this MMOItem UIFilter.
     */
    @NotNull public static MMOItemUIFilter get() { return global; }
    static MMOItemUIFilter global;
}
