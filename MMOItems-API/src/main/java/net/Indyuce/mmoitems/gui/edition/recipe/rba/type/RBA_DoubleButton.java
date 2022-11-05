package net.Indyuce.mmoitems.gui.edition.recipe.rba.type;

import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.QuickNumberRange;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.recipe.rba.RecipeButtonAction;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A button that stores a numeric value yes.
 *
 * @author Gunging
 */
public abstract class RBA_DoubleButton  extends RecipeButtonAction {

    /**
     * A button of an Edition Inventory. Nice!
     *
     * @param inv The edition inventory this is a button of
     */
    public RBA_DoubleButton(@NotNull RecipeMakerGUI inv) { super(inv); }

    /**
     * @return Straight from the file, if this option is set to TRUE.
     */
    public double getValue() { return getInv().getNameSection().getDouble(getDoubleConfigPath(), getDefaultValue()); }
    /**
     * @return The path to save this value in the config
     */
    @NotNull public abstract String getDoubleConfigPath();

    @NotNull public final String[] amountLog = {
            FriendlyFeedbackProvider.quickForPlayer(FFPMMOItems.get(), "Write in the chat a number, ex $e2.5$b.")};

    @NotNull public final String[] integerLog = {
            FriendlyFeedbackProvider.quickForPlayer(FFPMMOItems.get(), "Write in the chat an integer number, ex $e8$b.")};

    @Override public boolean runPrimary() {

        // Query user for input
        new StatEdition(getInv(), ItemStats.CRAFTING, RecipeMakerGUI.PRIMARY, this).enable(requireInteger() ? integerLog : amountLog);

        // Success
        return true;
    }

    /**
     * The user needs to input nothing; Thus this method never runs.
     *
     * @param message Input from the user
     * @param info Additional objects, specific to each case, provided.
     *
     * @throws IllegalArgumentException Never
     */
    @Override public void primaryProcessInput(@NotNull String message, Object... info) throws IllegalArgumentException {

        Double number;
        if (requireInteger()) {

            // Parse Integer
            Integer asInteger = SilentNumbers.IntegerParse(message);
            if (asInteger == null) { throw new IllegalArgumentException("Expected integer number instead of $u" + message); }

            // ...
            number = Double.valueOf(asInteger);
        } else {

            // Parse Double
            number = SilentNumbers.DoubleParse(message);
            if (number == null) { throw new IllegalArgumentException("Expected a number instead of $u" + message); }
        }

        // Out of range?
        if (getRange() != null) {

            // Out of range?
            if (!getRange().inRange(number)) {

                throw new IllegalArgumentException("Number $r" + number + "$b is out of range. Expected " + getRange().toStringColored());
            } }

        // Set value
        getInv().getNameSection().set(getDoubleConfigPath(), number);
    }

    @Nullable public abstract QuickNumberRange getRange();
    public abstract boolean requireInteger();

    @Override public boolean runSecondary() {

        // Remove value
        getInv().getNameSection().set(getDoubleConfigPath(), null);
        clickSFX();

        /*
         * Register template edition. This is only done automatically
         * on the input process methods, not on the run button ones.
         */
        getInv().registerTemplateEdition();

        // Done
        return true; }

    /**
     * The user needs to input nothing; Thus this method never runs.
     *
     * @param message Input from the user
     * @param info Additional objects, specific to each case, provided.
     *
     * @throws IllegalArgumentException Never
     */
    @SuppressWarnings("NoopMethodInAbstractClass")
    @Override public void secondaryProcessInput(@NotNull String message, Object... info) throws IllegalArgumentException { }

    public abstract double getDefaultValue();

    /**
     * @return The button ItemStack with its name and description. To
     *         it, all the chooseable values will be appended (as well
     *         as the definition of the current value chosen) when asked
     *         for in {@link #getButton()}
     */
    @NotNull public abstract ItemStack getDoubleButton();
    /**
     * @return Same as {@link #getDoubleButton()} but with
     *         the current value information appended to it.
     */
    @NotNull @Override public ItemStack getButton() {

        // Copy and send
        return RecipeMakerGUI.addLore(getDoubleButton().clone(),
                SilentNumbers.toArrayList(
                        "", "\u00a77Current Value: " + getValue(), "",
                        ChatColor.YELLOW + AltChar.listDash + " Right click to reset \u00a78(to\u00a74 " + getDefaultValue() + "\u00a78)\u00a7e.",
                        ChatColor.YELLOW + AltChar.listDash + " Left click to toggle this option." ));
    }
}
