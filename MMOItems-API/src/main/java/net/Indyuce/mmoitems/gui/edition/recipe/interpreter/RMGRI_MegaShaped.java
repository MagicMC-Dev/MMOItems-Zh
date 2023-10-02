package net.Indyuce.mmoitems.gui.edition.recipe.interpreter;

import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeEditorGUI;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is in charge of converting Shaped Recipes to and fro YML format,
 * as well as editing it in a YML configuration and such. <br> <br>
 *
 * YML Save Format: <br> <code>
 *
 *    - A|B|C|D|E|F <br>
 *    - G|H|I|J|K|L <br>
 *    - M|N|O|P|Q|R <br>
 *    - S|T|U|V|W|X <br>
 *    - Y|Z|1|2|3|4 <br>
 *    - 5|6|7|8|9|0
 * </code>
 *
 * @author Gunging
 */
public class RMGRI_MegaShaped implements RMG_RecipeInterpreter {


    /**
     * Builds a valid 6x6 matrix of input/output recipe.
     *
     * @param config List as it is saved in the config.
     *
     * @return Transcribed into array of arrays.
     */
    @NotNull
    ProvidedUIFilter[][] buildIngredientsFromList(@NotNull List<String> config) {

        // Start with a base
        ProvidedUIFilter[][] ret = new ProvidedUIFilter[6][6];

        // Each row ig
        for (int r = 0; r < 6; r++) {

            // Get current row
            String row = config.size() > r ? config.get(r) : null;
            //READ//MMOItems.log("\u00a7b*\u00a77 Reading\u00a7b " + row);

            // Update it ig
            String s = updateRow(row);
            //READ//MMOItems.log("\u00a7b*\u00a77 Updated to\u00a7b " + row);

            // Split
            String[] poofs = s.split("\\|");

            // Parse
            for (int p = 0; p < 6; p++) {

                String poof = poofs.length > p ? poofs[p] : null;
                //READ//MMOItems.log("\u00a7b*\u00a77 Coord\u00a7b " + r + " " + p + "\u00a77 as\u00a73 " + poof);

                // Parse
                ProvidedUIFilter parsed = ProvidedUIFilter.getFromString(poof, null);
                if (parsed == null) { parsed = RecipeEditorGUI.AIR.clone(); }

                // Add
                ret[r][p] = parsed; } }

        // And that's your result
        return ret;
    }
    /**
     * Turns something like <br> <code>
     *
     *     [ A, B, C, D, E, F ], <br>
     *     [ G, H, I, J, K, L ], <br>
     *     [ M, N, O, P, Q, R ], <br>
     *     [ S, T, U, V, W, X ], <br>
     *     [ Y, Z, 1, 2, 3, 4 ], <br>
     *     [ 5, 6, 7, 8, 9, 0 ]  <br>
     *
     * </code> <br>
     * into <br> <code>
     *
     *    - A|B|C|D|E|F <br>
     *    - G|H|I|J|K|L <br>
     *    - M|N|O|P|Q|R <br>
     *    - S|T|U|V|W|X <br>
     *    - Y|Z|1|2|3|4 <br>
     *    - 5|6|7|8|9|0
     * </code>
     *
     * @param ingredients Array of arrays of UIFilters
     *
     * @return A list of strings to save in a YML Config
     */
    @NotNull
    ArrayList<String> toYML(@NotNull ProvidedUIFilter[][] ingredients) {

        // Well, build it would ye?
        ArrayList<String> ret = new ArrayList<>();

        for (int r = 0; r < 6; r++) {

            // Get row
            ProvidedUIFilter[] poofs = ingredients.length > r ? ingredients[r] : new ProvidedUIFilter[5];

            // Concatenate
            StringBuilder sb = new StringBuilder();

            // Build
            for (ProvidedUIFilter poof : poofs) {
                ProvidedUIFilter providedUIFilter = poof;
                if (providedUIFilter == null) { providedUIFilter = RecipeEditorGUI.AIR.clone(); }

                // Add bar
                if (sb.length() != 0) { sb.append("|"); }

                // Add poof
                sb.append(providedUIFilter);
            }

            ret.add(sb.toString());
        }

        return ret;
    }

    @NotNull final ProvidedUIFilter[][] inputRecipe;
    /**
     * Sets the ingredient in the rows matrix.
     *
     * @param slot The slot, which must be between 0 and 35  (or this method will do nothing)
     * @param poof Ingredient to register
     */
    public void setInput(int slot, @NotNull ProvidedUIFilter poof) {
        if (slot < 0 || slot > 35) { return; }
        inputRecipe[SilentNumbers.floor(slot / 6.0)][slot - (6 * SilentNumbers.floor(slot / 6.0))] = poof;
    }
    @Nullable
    @Override public ProvidedUIFilter getInput(int slot) {
        if (slot < 0 || slot > 35) { return null; }
        return inputRecipe[SilentNumbers.floor(slot / 6.0)][slot - (6 * SilentNumbers.floor(slot / 6.0))];
    }

    @NotNull final ProvidedUIFilter[][] outputRecipe;
    /**
     * Sets the ingredient in the rows matrix.
     *
     * @param slot The slot, which must be between 0 and 35  (or this method will do nothing)
     * @param poof Ingredient to register
     */
    public void setOutput(int slot, @NotNull ProvidedUIFilter poof) {
        if (slot < 0 || slot > 35) { return; }
        outputRecipe[SilentNumbers.floor(slot / 6.0)][slot - (6 * SilentNumbers.floor(slot / 6.0))] = poof;
    }
    @Nullable
    @Override public ProvidedUIFilter getOutput(int slot) {
        if (slot < 0 || slot > 35) { return null; }
        return outputRecipe[SilentNumbers.floor(slot / 6.0)][slot - (6 * SilentNumbers.floor(slot / 6.0))];
    }

    @NotNull final ConfigurationSection section;
    /**
     * @return The recipe name section of this recipe. <br>
     *         <br>
     *         Basically <b><code>[ID].base.crafting.shaped.[name]</code></b> section
     */
    @NotNull public ConfigurationSection getSection() { return section; }

    /**
     * Generate an interpreter from this <i>updated</i> configuration section.
     *
     * @param recipeNameSection <b><code>[ID].base.crafting.shaped.[name]</code></b> section
     */
    public RMGRI_MegaShaped(@NotNull ConfigurationSection recipeNameSection) {

        // Save
        section = recipeNameSection;

        // Build Input list
        inputRecipe = buildIngredientsFromList(section.getStringList(RecipeEditorGUI.INPUT_INGREDIENTS));
        outputRecipe = buildIngredientsFromList(section.getStringList(RecipeEditorGUI.OUTPUT_INGREDIENTS));
    }

    @Override
    public void editInput(@NotNull ProvidedUIFilter input, int slot) {

        // Just edit bro
        setInput(slot, input);

        // Save
        section.set(RecipeEditorGUI.INPUT_INGREDIENTS, toYML(inputRecipe));
    }

    @Override
    public void editOutput(@NotNull ProvidedUIFilter input, int slot) {

        // Just edit bro
        setOutput(slot, input);

        // Save
        section.set(RecipeEditorGUI.OUTPUT_INGREDIENTS, toYML(outputRecipe));
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
    @NotNull public static String updateRow(@Nullable String curr) {
        if (curr == null || curr.isEmpty()) { return emptyRow;}

        // Bars used? I guess we can check that its written correctly
        if (curr.contains("|")) {

            // Split by bars
            String[] curSplit = curr.split("\\|");

            // Correct length?
            if (curSplit.length == 6) {

                // Assumed to be updated.
                return curr;

            } else {

                // Make sure it is of size three
                StringBuilder ret = new StringBuilder();

                // Must append three
                for (int r = 0; r < 6; r++) {

                    // Append a bar after the first
                    if (r != 0) { ret.append("|"); }

                    // Array has it?
                    if (r < curSplit.length) { ret.append(RecipeEditorGUI.poofFromLegacy(curSplit[r])); } else { ret.append("v AIR 0"); }
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
            for (int r = 0; r < 6; r++) {

                // Append a bar after the first
                if (r != 0) { ret.append("|"); }

                // Array has it?
                if (r < curSplit.length) { ret.append(RecipeEditorGUI.poofFromLegacy(curSplit[r])); } else { ret.append("v AIR 0"); }
            }

            // Build and return
            return ret.toString();

        // No spaces nor bars, this will just be the first ingredient of the row I guess
        } else {

            // Just that i guess
            return RecipeEditorGUI.poofFromLegacy(curr) + "|v AIR 0|v AIR 0|v AIR 0|v AIR 0|v AIR 0";
        }
    }
    public static final String emptyRow = "v AIR 0|v AIR 0|v AIR 0|v AIR 0|v AIR 0|v AIR 0";
    //endregion
}
