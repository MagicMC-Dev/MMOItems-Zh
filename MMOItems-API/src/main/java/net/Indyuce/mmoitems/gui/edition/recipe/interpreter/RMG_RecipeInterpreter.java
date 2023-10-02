package net.Indyuce.mmoitems.gui.edition.recipe.interpreter;

import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * When the user inputs an ingredient, after clicking the target slot number,
 * it is the job of the Recipe Interpreter to edit the ConfigurationSection.
 *
 * @author Gunging
 */
public interface RMG_RecipeInterpreter {

    /**
     * Edits the configuration section's INPUT list.
     *
     * @param input The user's input, item that will be required
     * @param slot  Slot that the item is going into
     */
    void editInput(@NotNull ProvidedUIFilter input, int slot);

    /**
     * Edits the configuration section's OUTPUT list.
     *
     * @param input The user's input, item that will be required
     * @param slot  Slot that the item is going into
     */
    void editOutput(@NotNull ProvidedUIFilter input, int slot);

    /**
     * Edits the configuration section's INPUT list.
     *
     * @param slot Slot that is getting reset
     */
    void deleteInput(int slot);

    /**
     * Edits the configuration section's OUTPUT list.
     *
     * @param slot Slot that is getting reset
     */
    void deleteOutput(int slot);

    /**
     * Fetch the Provided UI Filter in the YML configuration
     * that corresponds to this slot of the input.
     *
     * @param slot Slot
     * @return Identified filter, if found and valid.
     */
    @Nullable
    ProvidedUIFilter getInput(int slot);

    /**
     * Fetch the Provided UI Filter in the YML configuration
     * that corresponds to this slot of the output.
     *
     * @param slot Slot
     * @return Identified filter, if found and valid.
     */
    @Nullable
    ProvidedUIFilter getOutput(int slot);
}
