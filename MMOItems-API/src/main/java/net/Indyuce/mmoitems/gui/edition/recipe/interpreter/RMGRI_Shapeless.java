package net.Indyuce.mmoitems.gui.edition.recipe.interpreter;

import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is in charge of converting Shapeless Recipes to and fro YML format,
 * as well as editing it in a YML configuration and such. <br> <br>
 *
 * YML Save Format: <br> <code>
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
 * @author Gunging
 */
public class RMGRI_Shapeless implements RMG_RecipeInterpreter {

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
            String poof = RecipeMakerGUI.poofFromLegacy(row);

            // Parse
            ProvidedUIFilter parsed = ProvidedUIFilter.getFromString(poof, null);
            if (parsed == null) { parsed = RecipeMakerGUI.AIR.clone(); }

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
            ProvidedUIFilter poof = ingredients.length > r ? ingredients[r] : RecipeMakerGUI.AIR.clone();

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

    @NotNull final ConfigurationSection section;
    /**
     * @return The recipe name section of this recipe. <br>
     *         <br>
     *         Basically <b><code>[ID].base.crafting.shapeless.[name]</code></b> section
     */
    @NotNull public ConfigurationSection getSection() { return section; }

    /**
     * Generate an interpreter from this <i>updated</i> configuration section.
     * <br><br>
     * By 'updated' I mean that, for now, we <b>should call {@link RecipeMakerGUI#moveInput(ConfigurationSection, String)}
     * on this configuration before passing it here</b>, to move the input list from being the recipe name
     * section itself to the 'input' section within.
     *
     * @param recipeNameSection <b><code>[ID].base.crafting.shapeless.[name]</code></b> section
     */
    public RMGRI_Shapeless(@NotNull ConfigurationSection recipeNameSection) {

        // Save
        section = recipeNameSection;

        // Build Input list
        inputRecipe = buildIngredientsFromList(section.getStringList(RecipeMakerGUI.INPUT_INGREDIENTS));
        outputRecipe = buildIngredientsFromList(section.getStringList(RecipeMakerGUI.OUTPUT_INGREDIENTS));
    }

    @Override
    public void editInput(@NotNull ProvidedUIFilter input, int slot) {

        // Just edit bro
        setInput(slot, input);

        // Save
        section.set(RecipeMakerGUI.INPUT_INGREDIENTS, toYML(inputRecipe));
    }

    @Override
    public void editOutput(@NotNull ProvidedUIFilter input, int slot) {

        // Just edit bro
        setOutput(slot, input);

        // Save
        section.set(RecipeMakerGUI.OUTPUT_INGREDIENTS, toYML(outputRecipe));
    }

    @Override public void deleteInput(int slot) { editInput(RecipeMakerGUI.AIR.clone(), slot); }

    @Override public void deleteOutput(int slot) { editOutput(RecipeMakerGUI.AIR.clone(), slot); }
}
