package net.Indyuce.mmoitems.gui.edition.recipe.gui;

import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.gui.Navigator;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_HideFromBook;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_InputOutput;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.RecipeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Edits shapeless recipes, very nice.
 * <br> <br> <code>
 * - - - - - - - - -  <br>
 * 0 1 2 = 0 1 2 - -  <br>
 * 3 4 5 = 3 4 5 - R  <br>
 * 6 7 8 = 6 7 8 - -  </code>
 *
 * @author Gunging
 */
public class RMG_Shapeless extends RecipeEditorGUI {

    @NotNull
    final HashMap<Integer, Integer> inputLinks = new HashMap<>();

    /**
     * An editor for a Shapeless Recipe. Because the recipe is loaded from the YML when this is created,
     * concurrent modifications of the same recipe are unsupported.
     *
     * @param navigator  Current UI navigator
     * @param template   Template of which a recipe is being edited
     * @param recipeName Name of this recipe
     */
    public RMG_Shapeless(@NotNull Navigator navigator, @NotNull MMOItemTemplate template, @NotNull String recipeName, @NotNull RecipeRegistry recipeRegistry) {
        super(navigator, template, recipeName, recipeRegistry);
        addButton(new RBA_InputOutput(this));
        addButton(new RBA_HideFromBook(this));

        // Build Input list
        var section = recipeSection();
        inputRecipe = buildIngredientsFromList(section.getStringList(RecipeEditorGUI.INPUT_INGREDIENTS));
        outputRecipe = buildIngredientsFromList(section.getStringList(RecipeEditorGUI.OUTPUT_INGREDIENTS));

        // Bind inputs
        inputLinks.put(30, 0);
        inputLinks.put(31, 1);
        inputLinks.put(32, 2);

        inputLinks.put(39, 3);
        inputLinks.put(40, 4);
        inputLinks.put(41, 5);

        inputLinks.put(48, 6);
        inputLinks.put(49, 7);
        inputLinks.put(50, 8);
    }

    @Override
    public int getButtonsRow() {
        return 1;
    }

    @Override
    public void putRecipe() {

        // Fill inputs
        for (Integer s : inputLinks.keySet()) {
            inventory.setItem(s, getDisplay(isShowingInput(), inputLinks.get(s)));
        }
    }

    @Override
    int getInputSlot(int absolute) {

        // Not an input? Not our business
        @Nullable Integer found = inputLinks.get(absolute);

        // Found or negative
        return found != null ? found : -1;
    }

    //region Recipe interpreter


    /**
     * Builds a valid 3x3 matrix of input/output recipe.
     *
     * @param config List as it is saved in the config.
     *
     * @return Transcribed into array of arrays.
     */
    @NotNull ProvidedUIFilter[] buildIngredientsFromList(@NotNull List<String> config) {

        // Start with a base
        ProvidedUIFilter[] ret = new ProvidedUIFilter[9];

        // Each row ig
        for (int r = 0; r < 9; r++) {

            // Get current row
            String row = config.size() > r ? config.get(r) : null;

            // Update it ig
            String poof = RecipeEditorGUI.poofFromLegacy(row);

            // Parse
            ProvidedUIFilter parsed = ProvidedUIFilter.getFromString(poof, null);
            if (parsed == null) { parsed = RecipeEditorGUI.AIR.clone(); }

            // Add
            ret[r] = parsed;
        }

        // And that's your result
        return ret;
    }
    /**
     * Turns something like <br> <code>
     *
     *     [ A, B, C, D, E, F, G, H, I ]  <br>
     *
     * </code> <br>
     * into <br> <code>
     *
     *    - A <br>
     *    - B <br>
     *    - C <br>
     *    - D <br>
     *    - E <br>
     *    - F <br>
     *    - G <br>
     *    - H <br>
     *    - I <br>
     * </code>
     *
     * @param ingredients Array of arrays of UIFilters
     *
     * @return A list of strings to save in a YML Config
     */
    @NotNull ArrayList<String> toYML(@NotNull ProvidedUIFilter[] ingredients) {

        // Well, build it would ye?
        ArrayList<String> ret = new ArrayList<>();

        for (int r = 0; r < 9; r++) {

            // Get row
            ProvidedUIFilter poof = ingredients.length > r ? ingredients[r] : RecipeEditorGUI.AIR.clone();

            // Add poof
            ret.add(poof.toString());
        }

        // Thats it
        return ret;
    }

    @NotNull final ProvidedUIFilter[] inputRecipe;
    /**
     * Sets the ingredient in the rows matrix.
     *
     * @param slot The slot, which must be between 0 and 8  (or this method will do nothing)
     * @param poof Ingredient to register
     */
    public void setInput(int slot, @NotNull ProvidedUIFilter poof) {
        if (slot < 0 || slot > 8) { return; }
        inputRecipe[slot] = poof;
    }
    @Nullable
    @Override public ProvidedUIFilter getInput(int slot) {
        if (slot < 0 || slot > 8) { return null; }
        return inputRecipe[slot];
    }

    @NotNull final ProvidedUIFilter[] outputRecipe;
    /**
     * Sets the ingredient in the rows matrix.
     *
     * @param slot The slot, which must be between 0 and 8  (or this method will do nothing)
     * @param poof Ingredient to register
     */
    public void setOutput(int slot, @NotNull ProvidedUIFilter poof) {
        if (slot < 0 || slot > 8) { return; }
        outputRecipe[slot] = poof;
    }
    @Nullable
    @Override public ProvidedUIFilter getOutput(int slot) {
        if (slot < 0 || slot > 8) { return null; }
        return outputRecipe[slot];
    }

    @Override
    public void editInput(@NotNull ProvidedUIFilter input, int slot) {

        // Just edit bro
        setInput(slot, input);

        // Save
        recipeSection().set(RecipeEditorGUI.INPUT_INGREDIENTS, toYML(inputRecipe));
    }

    @Override
    public void editOutput(@NotNull ProvidedUIFilter input, int slot) {

        // Just edit bro
        setOutput(slot, input);

        // Save
        recipeSection().set(RecipeEditorGUI.OUTPUT_INGREDIENTS, toYML(outputRecipe));
    }

    @Override public void deleteInput(int slot) { editInput(RecipeEditorGUI.AIR.clone(), slot); }

    @Override public void deleteOutput(int slot) { editOutput(RecipeEditorGUI.AIR.clone(), slot); }

    //endregion

}
