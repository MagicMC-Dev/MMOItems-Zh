package net.Indyuce.mmoitems.gui.edition.recipe.interpreter;

import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * The legacy recipes that are not supported by MythicLib that all happen to have to do
 * with burning stuff - furnaces, campfires, the other furnaces...
 *
 * @author Gunging
 */
public class RMGRI_BurningLegacy implements RMG_RecipeInterpreter{

    /**
     * Interestingly enough, they onl require one input.
     */
    @NotNull ProvidedUIFilter input;
    /**
     * @return The stuff that must be smelted / cooked
     */
    @NotNull public ProvidedUIFilter getInput() { return input; }
    /**
     * Setting it to null will make it into AIR tho but ok.
     * This method does not update it in the Config Files.
     *
     * @param input The stuff that must be smelted
     */
    public void setInput(@Nullable ProvidedUIFilter input) { this.input = input == null ? RecipeMakerGUI.AIR : input; }

    @NotNull final ConfigurationSection section;
    /**
     * @return The recipe name section of this recipe. <br>
     *         <br>
     *         Basically <b><code>[ID].base.crafting.shaped.[name]</code></b> section
     */
    @NotNull public ConfigurationSection getSection() { return section; }

    /**
     * Generate an interpreter from this configuration section.
     *
     * @param recipeNameSection <b><code>[ID].base.crafting.furnace.[name]</code></b> section
     */
    public RMGRI_BurningLegacy(@NotNull ConfigurationSection recipeNameSection) {

        // Save
        section = recipeNameSection;

        // Furnaces support only input
        //noinspection ConstantConditions
        input = ProvidedUIFilter.getFromString(RecipeMakerGUI.poofFromLegacy(recipeNameSection.getString(ITEM)), null);
        if (input == null) { input = RecipeMakerGUI.AIR.clone(); }
    }

    @Override
    public void editInput(@NotNull ProvidedUIFilter input, int slot) {

        if (slot != 0) { return; }

        // Just edit bro
        setInput(input);

        // Save
        section.set(ITEM, input.toString());
    }

    @Override public void editOutput(@NotNull ProvidedUIFilter input, int slot) { }

    @Override public void deleteInput(int slot) { editInput(RecipeMakerGUI.AIR.clone(), slot); }

    @Override public void deleteOutput(int slot) { }

    @Nullable @Override public ProvidedUIFilter getInput(int slot) { if (slot == 0) { return input; } return null; }

    @Nullable @Override public ProvidedUIFilter getOutput(int slot) { return null; }

    public static final String ITEM = "item";
    public static final String TIME = "time";
    public static final String EXPERIENCE = "experience";
}
