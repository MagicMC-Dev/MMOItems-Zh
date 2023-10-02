package net.Indyuce.mmoitems.gui.edition.recipe.interpreter;

import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeEditorGUI;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This class is in charge of converting Smithing Recipes to and fro YML format,
 * as well as editing it in a YML configuration and such. <br> <br>
 *
 * YML Save Format: <br> <code>
 *
 *    - A|B <br>
 * </code>
 *
 * @author Gunging
 */
public class RMGRI_Smithing implements RMG_RecipeInterpreter {

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


    @NotNull final ConfigurationSection section;
    /**
     * @return The recipe name section of this recipe. <br>
     *         <br>
     *         Basically <b><code>[ID].base.crafting.shaped.[name]</code></b> section
     */
    @NotNull public ConfigurationSection getSection() { return section; }

    /**
     * Generate an interpreter from this <i>updated</i> configuration section.
     * <br><br>
     * By 'updated' I mean that, for now, we <b>should call {@link RecipeEditorGUI#moveInput()}
     * on this configuration before passing it here</b>, to move the input list from being the recipe name
     * section itself to the 'input' section within.
     *
     * @param recipeNameSection <b><code>[ID].base.crafting.shaped.[name]</code></b> section
     */
    public RMGRI_Smithing(@NotNull ConfigurationSection recipeNameSection) {

        // Save
        section = recipeNameSection;

        /*
         * Read input and output from the file
         */
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
    public void editInput(@NotNull ProvidedUIFilter input, int slot) {

        // Just edit bro
        setInput(slot, input);

        // Save
        section.set(RecipeEditorGUI.INPUT_INGREDIENTS, toYML(getInputItem(), getInputIngot()));
    }

    @Override
    public void editOutput(@NotNull ProvidedUIFilter input, int slot) {

        // Just edit bro
        setOutput(slot, input);

        // Save
        section.set(RecipeEditorGUI.OUTPUT_INGREDIENTS, toYML(getOutputItem(), getOutputIngot()));
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
}
