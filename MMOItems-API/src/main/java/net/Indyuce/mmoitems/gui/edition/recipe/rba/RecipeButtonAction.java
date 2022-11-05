package net.Indyuce.mmoitems.gui.edition.recipe.rba;

import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * When the user clicks the Edition Inventory of a Recipe Maker,
 * it might be a special button specific to a recipe. In such a
 * case, RMGs may register all their buttons as RBAs, and perform
 * operations with low maintenance.
 *
 * @author Gunging
 */
public abstract class RecipeButtonAction {

    /**
     * The edition inventory this is a button of
     */
    @NotNull final RecipeMakerGUI inv;
    /**
     * @return The edition inventory this is a button of
     */
    @NotNull public RecipeMakerGUI getInv() { return inv; }

    /**
     * A button of an Edition Inventory. Nice!
     *
     * @param inv The edition inventory this is a button of
     */
    public RecipeButtonAction(@NotNull RecipeMakerGUI inv) { this.inv = inv; }

    /**
     * Called when the player left-clicks a slot. <br>
     * <b>Important: When initializing a {@link StatEdition#StatEdition(EditionInventory, ItemStat, Object...)} you
     * must pass {@link RecipeMakerGUI#PRIMARY} as the first <i>info</i> object!</b> Also, make sure to pass {@code this}
     * as the second argument for {@link #primaryProcessInput(String, Object...)} to be called.
     *
     * @return <code>true</code> if and only if this action succeeded. Most importantly,
     *         indicates that the absolute slot the user clicked corresponds to this
     *         button being clicked.
     */
    public abstract boolean runPrimary();
    /**
     * Run the function performed by this button, based on the user's input.
     *
     * This will be called when {@link #runPrimary()} (int, EditionInventory)} succeeds and calls
     * {@link StatEdition#StatEdition(EditionInventory, ItemStat, Object...)}
     * to query the user for input.
     *
     * @param message Input from the user
     * @param info Additional objects, specific to each case, provided.
     *
     * @throws IllegalArgumentException If anything goes wrong.
     */
    public abstract void primaryProcessInput(@NotNull String message, Object... info) throws IllegalArgumentException;

    /**
     * Called when the player right-clicks a slot. <br>
     * <b>Important: When initializing a {@link StatEdition#StatEdition(EditionInventory, ItemStat, Object...)} you
     * must pass {@link RecipeMakerGUI#SECONDARY} as the first <i>info</i> object!</b> Also, make sure to pass {@code this}
     * as the second argument for {@link #secondaryProcessInput(String, Object...)} to be called.
     *
     * @return <code>true</code> if and only if this action succeeded. Most importantly,
     *         indicates that the absolute slot the user clicked corresponds to this
     *         button being clicked.
     */
    public abstract boolean runSecondary();
    /**
     * Run the function performed by this button, based on the user's input.
     *
     * This will be called when {@link #runSecondary()} succeeds and calls
     * {@link StatEdition#StatEdition(EditionInventory, ItemStat, Object...)}
     * to query the user for input.
     *
     * @param message Input from the user
     * @param info Additional objects, specific to each case, provided.
     *
     * @throws IllegalArgumentException If anything goes wrong.
     */
    public abstract void secondaryProcessInput(@NotNull String message, Object... info) throws IllegalArgumentException;

    /**
     * @return The ItemStack that will act as the button in the GUI, to activate this class.
     */
    @NotNull public abstract ItemStack getButton();

    /**
     * Plays a clicking sound
     */
    public void clickSFX() { getInv().getPlayer().playSound(getInv().getPlayer().getLocation(), Sound.UI_BUTTON_CLICK, 1, 1); }
}
