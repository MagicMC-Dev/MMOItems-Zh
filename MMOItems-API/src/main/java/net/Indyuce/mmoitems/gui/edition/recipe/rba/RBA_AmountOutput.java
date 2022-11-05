package net.Indyuce.mmoitems.gui.edition.recipe.rba;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.util.AltChar;
import io.lumine.mythic.lib.api.util.ItemFactory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * The user will specify the output amount of a recipe on
 * a per-recipe basis, and will use this button for that.
 *
 * @author Gunging
 */
public class RBA_AmountOutput extends RecipeButtonAction {

    /**
     * The button that displays how much output this recipe will produce.
     *
     * @param inv Inventory this button is part of
     * @param resultItem Output item of this recipe
     */
    public RBA_AmountOutput(@NotNull RecipeMakerGUI inv, @NotNull ItemStack resultItem) {
        super(inv);

        // Get item
        button = RecipeMakerGUI.rename(ItemFactory.of(resultItem.getType()).lore(SilentNumbers.chop(
                "The amount of items produced every time the player crafts."
                , 65, "\u00a77")).build(), "\u00a7cChoose Output Amount");


        // Update CMD ~ The stupid warning for 'ItemMEta might be false' is so annoying bruh
        @NotNull ItemMeta buttonMeta = Objects.requireNonNull(button.getItemMeta());
        @NotNull ItemMeta resultMeta = Objects.requireNonNull(resultItem.getItemMeta());

        if (MythicLib.plugin.getVersion().isStrictlyHigher(1, 13) && resultMeta.hasCustomModelData()) { buttonMeta.setCustomModelData(resultMeta.getCustomModelData()); }
        if (resultMeta instanceof LeatherArmorMeta) { ((LeatherArmorMeta) buttonMeta).setColor(((LeatherArmorMeta) resultMeta).getColor()); }
        if (resultMeta instanceof BannerMeta) { ((BannerMeta) buttonMeta).setPatterns(((BannerMeta) resultMeta).getPatterns()); }
        buttonMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DYE, ItemFlag.HIDE_POTION_EFFECTS);
        button.setItemMeta(buttonMeta);
    }

    @NotNull public final String[] amountLog = {
            FriendlyFeedbackProvider.quickForPlayer(FFPMMOItems.get(), "Write in the chat the amount of output of this recipe."),
            FriendlyFeedbackProvider.quickForPlayer(FFPMMOItems.get(), "It must be an integer number, ex $e4$b.")};

    /**
     * When the player clicks the display item, it means they want to change
     * the amount output of this recipe. Thus, they are queried to write
     * an integer number in the chat.
     *
     * @return If the player clicked the display item
     */
    @Override public boolean runPrimary() {

        // Query user for input
        new StatEdition(inv, ItemStats.CRAFTING, RecipeMakerGUI.PRIMARY, this).enable(amountLog);

        // Success
        return true;
    }
    /**
     * The player has written a number that will be set as the output amount of this recipe.
     * <br>
     * The amount is saved in YML path {@code [ID].crafting.[recipe].[name].amount}
     *
     * @param message Input from the user
     * @param info Additional objects, specific to each case, provided.
     *
     * @throws IllegalArgumentException If the player did not write an integer number
     */
    @Override public void primaryProcessInput(@NotNull String message, Object... info) throws IllegalArgumentException {

        // Parse
        Integer val = SilentNumbers.IntegerParse(message);
        if (val == null) { throw new IllegalArgumentException("Expected an integer number instead of $u" + message); }
        if (val > 64) { throw new IllegalArgumentException("Max stack size is $e64$b, Minecraft doesnt support $u" + message); }
        if (val <= 0) { throw new IllegalArgumentException("Min output stack size is $e0$b, you specified $u" + message); }

        // Set value
        getInv().getNameSection().set(AMOUNT_INGREDIENTS, val);
    }

    /**
     * If the player left-clicks the display item, the behaviour is just reset
     * the amount to 1 output. No need to query the user.
     *
     * @return If the player clicked the display item
     */
    @Override public boolean runSecondary() {

        // Set value
        getInv().getNameSection().set(AMOUNT_INGREDIENTS, null);
        clickSFX();

        /*
         * Register template edition. This is only done automatically
         * on the input process methods, not on the run button ones.
         */
        inv.registerTemplateEdition();

        // Done
        return true;
    }

    /**
     * @return Straight from the file, the amount output of this recipe.
     */
    public int getOutputAmount() { return getInv().getNameSection().getInt(AMOUNT_INGREDIENTS, 1); }

    /**
     * The user needs to input nothing; Thus this method never runs.
     *
     * @param message Input from the user
     * @param info Additional objects, specific to each case, provided.
     *
     * @throws IllegalArgumentException Never
     */
    @Override public void secondaryProcessInput(@NotNull String message, Object... info) throws IllegalArgumentException { }

    @NotNull final ItemStack button;

    @Override
    @NotNull public ItemStack getButton() {

        // Dupe and change amount
        ItemStack ret = button.clone();
        ret.setAmount(getOutputAmount());

        // That's it
        return RecipeMakerGUI.addLore(ret, SilentNumbers.toArrayList( "",
                ChatColor.YELLOW + AltChar.listDash + " Right click to reset to 1.",
                ChatColor.YELLOW + AltChar.listDash + " Left click to edit amount." ));
    }

    public static final String AMOUNT_INGREDIENTS = "amount";
}
