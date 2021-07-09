package net.Indyuce.mmoitems.api.crafting.ingredient.inventory;


import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.crafting.ConditionalDisplay;
import net.Indyuce.mmoitems.api.crafting.ingredient.CheckedIngredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.Ingredient;
import net.Indyuce.mmoitems.api.crafting.ingredient.IngredientType;
import net.Indyuce.mmoitems.api.crafting.recipe.CheckedRecipe;
import net.Indyuce.mmoitems.manager.CraftingManager;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Code architecture for all the classes concerning Ingredients.
 * <p>
 * MMOItems based ingredients / vanilla based ingredients are called IngredientTypes.
 * One {@link IngredientType} is loaded for every type of ingredient there exists.
 * External plugins can register other types of ingredients using
 * {@link CraftingManager#registerIngredient(String, Function, ConditionalDisplay, Predicate, Function)}.
 * <p>
 * Once all the ingredient types are loaded, stations are loaded from the config files.
 * That means loading the ingredients from the recipes which is what {@link Ingredient} is used for.
 * <p>
 * When a player opens a crafting station, MMOItems must first calculate all the ingredients
 * they have in their inventory. A {@link IngredientInventory} is created, calculating all
 * the ingredients the player has using the {@link PlayerIngredient} interface.
 * <p>
 * The second step when opening a crafting station is comparing the ingredients required for one
 * crafting recipe to the current player's ingredients, which is done using {@link CheckedIngredient}
 * when creating {@link CheckedRecipe}.
 *
 * @author indyuce
 */
public abstract class PlayerIngredient {

    /**
     * Redirects to the player's item in his inventory. This can be
     * used later by MMOItems to reduce the amount of items in his inventory
     * to take off ingredients from him.
     */
    private final ItemStack item;

    public PlayerIngredient(NBTItem item) {

        // Throw NBTItem to garbage collector because no longer needed
        this.item = item.getItem();
    }

    public ItemStack getItem() {
        return item;
    }

    public int getAmount() {
        return item.getAmount();
    }
}
