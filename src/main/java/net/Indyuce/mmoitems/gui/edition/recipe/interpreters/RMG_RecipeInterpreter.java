package net.Indyuce.mmoitems.gui.edition.recipe.interpreters;

import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
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
     * @param section <b>The 'crafting' section -  [ID].base.crafting</b>
     *                <br><br>
     *                Note that this is not editing the recipe type nor name itself.
     *                It is up to the interpreter to create the sections if missing
     *                or edit them if they are already there.
     *                <br><br>
     *                Ex: [ID].base.crafting.shaped.1.input
     *
     * @param input The user's input, item that will be required
     *
     * @param slot Slot that the item is going into
     */
    void editInput(@NotNull ConfigurationSection section, @NotNull ProvidedUIFilter input, int slot);

    /**
     * Edits the configuration section's OUTPUT list.
     *
     * @param section <b>The 'crafting' section -  [ID].base.crafting</b>
     *                <br><br>
     *                Note that this is not editing the recipe type nor name itself.
     *                It is up to the interpreter to create the sections if missing
     *                or edit them if they are already there.
     *                <br><br>
     *                Ex: [ID].base.crafting.shaped.1.output
     *
     * @param input The user's input, item that will be required
     *
     * @param slot Slot that the item is going into
     */
    void editOutput(@NotNull ConfigurationSection section, @NotNull ProvidedUIFilter input, int slot);

    /**
     * Edits the configuration section's INPUT list.
     *
     * @param section <b>The 'crafting' section -  [ID].base.crafting</b>
     *                <br><br>
     *                Note that this is not editing the recipe type nor name itself.
     *                It is up to the interpreter to create the sections if missing
     *                or edit them if they are already there.
     *                <br><br>
     *                Ex: [ID].base.crafting.shaped.1.input
     *
     * @param slot Slot that is getting reset
     */
    void deleteInput(@NotNull ConfigurationSection section, int slot);

    /**
     * Edits the configuration section's OUTPUT list.
     *
     * @param section <b>The 'crafting' section -  [ID].base.crafting</b>
     *                <br><br>
     *                Note that this is not editing the recipe type nor name itself.
     *                It is up to the interpreter to create the sections if missing
     *                or edit them if they are already there.
     *                <br><br>
     *                Ex: [ID].base.crafting.shaped.1.output
     *
     * @param slot Slot that is getting reset
     */
    void deleteOutput(@NotNull ConfigurationSection section, int slot);

    /**
     * Fetch the Provided UI Filter in the YML configuration
     * that corresponds to this slot of the input.
     *
     * @param slot Slot
     *
     * @return Identified filter, if found and valid.
     */
    @Nullable ProvidedUIFilter getInput(int slot);

    /**
     * Fetch the Provided UI Filter in the YML configuration
     * that corresponds to this slot of the output.
     *
     * @param slot Slot
     *
     * @return Identified filter, if found and valid.
     */
    @Nullable ProvidedUIFilter getOutput(int slot);
}
