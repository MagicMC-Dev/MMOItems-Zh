package net.Indyuce.mmoitems.api.recipe;

import io.lumine.mythic.lib.api.crafting.uifilters.EnchantmentUIFilter;
import io.lumine.mythic.lib.api.crafting.uifilters.UIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.UIFilterManager;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.VolatileMMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
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

        // All right get its Type and ID
        if (!mmo.getType().getId().equals(argument) || !mmo.getId().equals(data)) {

            // Notify
            FriendlyFeedbackProvider.log(ffp, FriendlyFeedbackCategory.FAILURE,
                    "MMOItem $r{0} {1}$b is not the expected $r{2} {3}$b. $fNo Match. ",
                    mmo.getType().getId(), mmo.getId(), argument, data);

            // Fail
            return false;
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

        // Can find item?
        if (!MMOItems.plugin.getTemplates().hasTemplate(t, data)) {


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

            // Just filter among template names of this type
            return SilentNumbers.smartFilter(MMOItems.plugin.getTemplates().getTemplateNames(t), data, true);
            
        } else {

            // Typo in the Type
            return SilentNumbers.toArrayList("Type_not_found,_check_your_spelling");
        }
    }

    @Override
    public boolean fullyDefinesItem() { return true; }

    @Nullable
    @Override
    public ItemStack getItemStack(@NotNull String argument, @NotNull String data, @Nullable FriendlyFeedbackProvider ffp) {
        if (!isValid(argument, data, ffp)) { return null; }
        return MMOItems.plugin.getItem(argument, data);
    }

    @NotNull
    @Override
    public ArrayList<String> getDescription(@NotNull String argument, @NotNull String data) {

        // Check validity
        if (!isValid(argument, data, null)) { return SilentNumbers.toArrayList("This MMOItem is $finvalid$b."); }
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
