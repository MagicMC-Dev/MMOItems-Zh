package net.Indyuce.mmoitems.gui.edition.recipe.gui;

import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.gui.Navigator;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_DropGems;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_InputOutput;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_SmithingEnchantments;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_SmithingUpgrades;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.RecipeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Edits smithing recipes, very nice.
 * <br> <br> <code>
 * - - - - - - - - -  <br>
 * - - - = - - - - -  <br>
 * 0 - 1 = 0 - 1 - R  <br>
 * - - - = - - - - -  </code>
 *
 * @author Gunging
 */
public class RMG_Smithing extends RecipeEditorGUI {

    @NotNull
    final HashMap<Integer, Integer> inputLinks = new HashMap<>();

    /**
     * An editor for a Shaped Recipe. Because the recipe is loaded from the YML when this is created,
     * concurrent modifications of the same recipe are unsupported.
     *
     * @param navigator Current UI navigator
     * @param template Template of which a recipe is being edited
     * @param recipeName Name of this recipe
     */
    public RMG_Smithing(@NotNull Navigator navigator, @NotNull MMOItemTemplate template, @NotNull String recipeName, @NotNull RecipeRegistry recipeRegistry) {
        super(navigator, template, recipeName, recipeRegistry);

        // Read input and output from the file
        {
            var section = recipeSection();
            String input = updateIngredients(section.getString(RecipeEditorGUI.INPUT_INGREDIENTS));
            String output = updateIngredients(section.getString(RecipeEditorGUI.OUTPUT_INGREDIENTS));

            // Split
            String[] inputSplit = input.split("\\|");
            String[] outputSplit = output.split("\\|");

            ProvidedUIFilter inputItemParse = ProvidedUIFilter.getFromString(inputSplit[0], null);
            ProvidedUIFilter outputItemParse = ProvidedUIFilter.getFromString(outputSplit[0], null);
            ProvidedUIFilter inputIngotParse = ProvidedUIFilter.getFromString(inputSplit[1], null);
            ProvidedUIFilter outputIngotParse = ProvidedUIFilter.getFromString(outputSplit[1], null);

            // Build Input list
            inputItem = inputItemParse != null ? inputItemParse : RecipeEditorGUI.AIR.clone();
            inputIngot = inputIngotParse != null ? inputIngotParse : RecipeEditorGUI.AIR.clone();
            outputItem = outputItemParse != null ? outputItemParse : RecipeEditorGUI.AIR.clone();
            outputIngot = outputIngotParse != null ? outputIngotParse : RecipeEditorGUI.AIR.clone();
        }

        // Bind inputs
        inputLinks.put(39, 0);
        inputLinks.put(41, 1);

        // Extra buttons
        addButton(new RBA_InputOutput(this));
        addButton(new RBA_SmithingUpgrades(this));
        addButton(new RBA_SmithingEnchantments(this));
        addButton(new RBA_DropGems(this));
    }


    @Override
    public void putRecipe() {

        // Fill inputs
        for (Integer s : inputLinks.keySet()) { inventory.setItem(s, getDisplay(isShowingInput(), inputLinks.get(s))); }
    }

    @Override
    int getInputSlot(int absolute) {

        // Not an input? Not our business
        @Nullable Integer found = inputLinks.get(absolute);

        // Found or negative
        return found != null ? found : -1;
    }
    @Override public int getButtonsRow() { return 1; }

    //region Recipe interpreter


    /**
     * Turns something like <br> <code>
     *  [ A, B ]
     *
     * </code> <br> <br>
     * into <br> <code>
     *
     *    - A|B
     * </code>
     *
     * @param item first input
     * @param ingot second input
     *
     * @return A string to save in a YML Config
     */
    @NotNull String toYML(@NotNull ProvidedUIFilter item, @NotNull ProvidedUIFilter ingot) {

        // Well, build it would ye?
        return item + "|" + ingot;
    }

    @NotNull ProvidedUIFilter inputItem;
    @NotNull public ProvidedUIFilter getInputItem() { return inputItem; }
    public void setInputItem(@NotNull ProvidedUIFilter inputItem) { this.inputItem = inputItem; }

    @NotNull ProvidedUIFilter outputItem;
    @NotNull public ProvidedUIFilter getOutputItem() { return outputItem; }
    public void setOutputItem(@NotNull ProvidedUIFilter outputItem) { this.outputItem = outputItem; }

    @NotNull ProvidedUIFilter inputIngot;
    @NotNull public ProvidedUIFilter getInputIngot() { return inputIngot; }
    public void setInputIngot(@NotNull ProvidedUIFilter inputIngot) { this.inputIngot = inputIngot; }

    @NotNull ProvidedUIFilter outputIngot;
    @NotNull public ProvidedUIFilter getOutputIngot() { return outputIngot; }
    public void setOutputIngot(@NotNull ProvidedUIFilter outputIngot) { this.outputIngot = outputIngot; }

    /**
     * @param slot The slot, which must be between 0 and 8  (or this method will do nothing)
     * @param poof Ingredient to register
     */
    public void setInput(int slot, @NotNull ProvidedUIFilter poof) {
        if (slot == 0) { setInputItem(poof); } else if (slot == 1) { setInputIngot(poof); }
    }
    @Nullable
    @Override public ProvidedUIFilter getInput(int slot) {
        if (slot == 0) { return getInputItem(); } else if (slot == 1) { return getInputIngot(); }
        return null;
    }

    /**
     * @param slot The slot, which must be between 0 and 8  (or this method will do nothing)
     * @param poof Ingredient to register
     */
    public void setOutput(int slot, @NotNull ProvidedUIFilter poof) {
        if (slot == 0) { setOutputItem(poof); } else if (slot == 1) { setOutputIngot(poof); }
    }
    @Nullable
    @Override public ProvidedUIFilter getOutput(int slot) {
        if (slot == 0) { return getOutputItem(); } else if (slot == 1) { return getOutputIngot(); }
        return null;
    }

    @Override
    public void editInput( @NotNull ProvidedUIFilter input, int slot) {

        // Just edit bro
        setInput(slot, input);

        // Save
        recipeSection().set(RecipeEditorGUI.INPUT_INGREDIENTS, toYML(getInputItem(), getInputIngot()));
    }

    @Override
    public void editOutput( @NotNull ProvidedUIFilter input, int slot) {

        // Just edit bro
        setOutput(slot, input);

        // Save
        recipeSection().set(RecipeEditorGUI.OUTPUT_INGREDIENTS, toYML(getOutputItem(), getOutputIngot()));
    }

    @Override public void deleteInput(int slot) { editInput(RecipeEditorGUI.AIR.clone(), slot); }

    @Override public void deleteOutput(int slot) { editOutput(RecipeEditorGUI.AIR.clone(), slot); }

    //region Updater, to update old recipes
    /**
     * No matter what input, the output will always be three Provided UIFilters
     * separated by bars, as expected in the current system, filling with AIR
     * where necessary.
     *
     * @param curr Current string
     *
     * @return A row in correct format
     */
    @NotNull public static String updateIngredients(@Nullable String curr) {
        if (curr == null || curr.isEmpty()) { return emptyIngredients;}

        // Bars used? I guess we can check that its written correctly
        if (curr.contains("|")) {

            // Split by bars
            String[] curSplit = curr.split("\\|");

            // Correct length?
            if (curSplit.length == 2) {

                // Assumed to be updated.
                return curr;

            } else {

                // Make sure it is of size three
                StringBuilder ret = new StringBuilder();

                // Must append three
                for (int r = 0; r < 2; r++) {

                    // Append a bar after the first
                    if (r != 0) { ret.append("|"); }

                    // Array has it?
                    if (r < curSplit.length) { ret.append(RecipeEditorGUI.poofFromLegacy(curSplit[r])); } else { ret.append("v AIR -"); }
                }

                // Build and return
                return ret.toString();
            }

            // Not bars, but spaces, might be old format
        } else if (curr.contains(" ")) {

            // Make string builder
            StringBuilder ret = new StringBuilder();
            String[] curSplit = curr.split(" ");

            // Must append three
            for (int r = 0; r < 2; r++) {

                // Append a bar after the first
                if (r != 0) { ret.append("|"); }

                // Array has it?
                if (r < curSplit.length) { ret.append(RecipeEditorGUI.poofFromLegacy(curSplit[r])); } else { ret.append("v AIR -"); }
            }

            // Build and return
            return ret.toString();

            // No spaces nor bars, this will just be the first ingredient of the row I guess
        } else {

            // Just that i guess
            return RecipeEditorGUI.poofFromLegacy(curr) + "|v AIR 0";
        }
    }
    public static final String emptyIngredients = "v AIR -|v AIR -";
    //endregion

    //endregion

}
