package net.Indyuce.mmoitems.gui.edition.recipe.gui;

import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.gui.Navigator;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_CookingTime;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_Experience;
import net.Indyuce.mmoitems.gui.edition.recipe.button.RBA_HideFromBook;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.RecipeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;


/**
 * The legacy recipes that are not supported by MythicLib that all happen to have to do
 * with burning stuff - furnaces, campfires, the other furnaces...
 *
 * @author Gunging
 */
public class RMG_BurningLegacy extends RecipeEditorGUI {

    @NotNull
    HashMap<Integer, Integer> inputLinks = new HashMap<>();

    /**
     * An editor for a Shaped Recipe. Because the recipe is loaded from the YML when this is created,
     * concurrent modifications of the same recipe are unsupported.
     *
     * @param navigator  Current UI navigator
     * @param template   Template of which a recipe is being edited
     * @param recipeName Name of this recipe
     */
    public RMG_BurningLegacy(@NotNull Navigator navigator, @NotNull MMOItemTemplate template, @NotNull String recipeName, @NotNull RecipeRegistry recipeRegistry) {
        super(navigator, template, recipeName, recipeRegistry);
        addButton(new RBA_HideFromBook(this));
        addButton(new RBA_Experience(this));
        addButton(new RBA_CookingTime(this));

        // NO OUTPUT
        if (!isShowingInput()) {
            switchInput();
        }

        // Furnaces support only input
        //noinspection ConstantConditions
        input = ProvidedUIFilter.getFromString(RecipeEditorGUI.poofFromLegacy(recipeSection().getString(ITEM)), null);
        if (input == null) { input = RecipeEditorGUI.AIR.clone(); }

        // Bind inputs - Furnace only has which item to smelt
        inputLinks.put(40, 0);
    }

    @Override
    public int getButtonsRow() {
        return 2;
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

    //region interpreter

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
    public void setInput(@Nullable ProvidedUIFilter input) { this.input = input == null ? RecipeEditorGUI.AIR : input; }

    @Override
    public void editInput(@NotNull ProvidedUIFilter input, int slot) {

        if (slot != 0) { return; }

        // Just edit bro
        setInput(input);

        // Save
        recipeSection().set(ITEM, input.toString());
    }

    @Override public void editOutput(@NotNull ProvidedUIFilter input, int slot) { }

    @Override public void deleteInput(int slot) { editInput(RecipeEditorGUI.AIR.clone(), slot); }

    @Override public void deleteOutput(int slot) { }

    @Nullable @Override public ProvidedUIFilter getInput(int slot) { if (slot == 0) { return input; } return null; }

    @Nullable @Override public ProvidedUIFilter getOutput(int slot) { return null; }

    public static final String ITEM = "item";
    public static final String TIME = "time";
    public static final String EXPERIENCE = "experience";

    //endregion
}
