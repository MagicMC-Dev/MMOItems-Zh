package net.Indyuce.mmoitems.gui.edition.recipe.registry;

import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipeBlueprint;
import io.lumine.mythic.lib.api.util.Ref;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.burninglegacy.CraftingType;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.WorkbenchIngredient;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RMG_BurningLegacy;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
import net.Indyuce.mmoitems.gui.edition.recipe.rba.RBA_AmountOutput;
import net.Indyuce.mmoitems.gui.edition.recipe.rba.RBA_CookingTime;
import net.Indyuce.mmoitems.gui.edition.recipe.rba.RBA_Experience;
import net.Indyuce.mmoitems.gui.edition.recipe.rba.RBA_HideFromBook;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.burninglegacy.BurningRecipeInformation;
import net.Indyuce.mmoitems.manager.RecipeManager;
import org.apache.commons.lang.Validate;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Recipes for furnaces. Ive never worked with these,
 * and I don't claim to support them. They are just kinda
 * compatible with the new crafting GUI and THAT'S IT.
 *
 * Still using Aria's code.
 *
 * @author Gunging
 */
public abstract class RMGRR_LegacyBurning implements RecipeRegistry {

    @NotNull public abstract CraftingType getLegacyBurningType();

    @NotNull public static String capitalizeFirst(@NotNull String str) { return str.substring(0, 1).toUpperCase() + str.substring(1); }

    @NotNull @Override public String getRecipeConfigPath() { return getLegacyBurningType().name().toLowerCase(); }

    @NotNull @Override public String getRecipeTypeName() { return "§8{§4§oL§8} " + capitalizeFirst(getRecipeConfigPath()); }

    @SuppressWarnings("NotNullFieldNotInitialized")
    @NotNull
    private final ItemStack displayListItem = RecipeMakerGUI.rename(getLegacyBurningType().getItem(), FFPMMOItems.get().getExampleFormat() + capitalizeFirst(getRecipeConfigPath()) + " Recipe");

    @NotNull
    @Override
    public ItemStack getDisplayListItem() {
        return displayListItem;
    }

    @Override public void openForPlayer(@NotNull EditionInventory inv, @NotNull String recipeName, Object... otherParams) { new RMG_BurningLegacy(inv.getPlayer(), inv.getEdited(), recipeName, this).open(inv.getPreviousPage()); }

    /**
     * Actually doesnt really send this thing to MythicLib
     * its just guaranteed to throw an exception and uses
     * the legacy MMOItems way of registering.
     */
    @NotNull @Override public MythicRecipeBlueprint sendToMythicLib(@NotNull MMOItemTemplate template, @NotNull ConfigurationSection recipeTypeSection, @NotNull String recipeName, @NotNull Ref<NamespacedKey> namespace, @NotNull FriendlyFeedbackProvider ffp) throws IllegalArgumentException {

        // Never happening
        Validate.isTrue(namespace.getValue() != null);

        // Get correct section
        ConfigurationSection recipeSection = RecipeMakerGUI.getSection(recipeTypeSection, recipeName);

        // Get ingredient
        String itemIngredient = recipeSection.getString("item");
        if (itemIngredient == null) { throw new IllegalArgumentException("Missing input ingredient"); }
        WorkbenchIngredient ingredient = RecipeManager.getWorkbenchIngredient(itemIngredient);

        // Read amount from configuration
        int outputAmount = recipeSection.getInt(RBA_AmountOutput.AMOUNT_INGREDIENTS, 1);
        double experience = recipeSection.getDouble(RBA_Experience.FURNACE_EXPERIENCE, RBA_Experience.DEFAULT);
        int time = recipeSection.getInt(RBA_CookingTime.FURNACE_TIME, SilentNumbers.round(RBA_CookingTime.DEFAULT));
        boolean hideBook = recipeSection.getBoolean(RBA_HideFromBook.BOOK_HIDDEN, false);

        // Make that recipe yes
        BurningRecipeInformation info = new BurningRecipeInformation(ingredient, (float) experience, time);

        // Yes
        MMOItems.plugin.getRecipes().registerBurningRecipe(
                getLegacyBurningType().getBurningType(),
                template.newBuilder(0, null).build(),
                info, outputAmount, namespace.getValue(), hideBook);

        throw new IllegalArgumentException("");
    }
}
