package net.Indyuce.mmoitems.gui.edition.recipe.registry.burninglegacy;

import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.WorkbenchIngredient;
import net.Indyuce.mmoitems.manager.RecipeManager;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

/**
 * Used to handle furnace/smoker/campfire/furnace
 * extra crafting recipe parameters
 *
 * @author ASangarin
 */
@Deprecated
public class BurningRecipeInformation {
    private final WorkbenchIngredient choice;
    private final float exp;
    private final int burnTime;

    public BurningRecipeInformation(@NotNull ConfigurationSection config) {

        // Get item
        String itemIngredient = config.getString("item");
        if (itemIngredient == null) { throw new IllegalArgumentException("输入材料无效"); }

        // Get
        choice = RecipeManager.getWorkbenchIngredient(itemIngredient);
        exp = (float) config.getDouble("exp", 0.35);
        burnTime = config.getInt("time", 200);
    }

    public BurningRecipeInformation(@NotNull WorkbenchIngredient ingredient, float exp, int burnTime) {
        choice = ingredient;
        this.exp = exp;
        this.burnTime = burnTime;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public WorkbenchIngredient getChoice() {
        return choice;
    }

    public float getExp() {
        return exp;
    }
}
