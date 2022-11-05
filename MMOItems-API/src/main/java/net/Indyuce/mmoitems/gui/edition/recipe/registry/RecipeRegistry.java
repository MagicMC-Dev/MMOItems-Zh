package net.Indyuce.mmoitems.gui.edition.recipe.registry;

import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipeBlueprint;
import io.lumine.mythic.lib.api.util.Ref;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * With information on displaying and creating Recipes.
 *
 * @author Gunging
 */
public interface RecipeRegistry {

    /**
     * @return In item YML configurations, recipes are saved under
     *         <code>[ID].crafting.[recipe]</code>, where this string
     *         is the value of [recipe].
     *         <br>
     *         Ex. <code>STEEL_SWORD.crafting.shaped</code>
     */
    @NotNull String getRecipeConfigPath();

    /**
     * When making an inventory, the chest name reads '{@code Recipe Editor - ######}' <br>
     * This method retuns the value of that '######'.
     * <br><br>
     * For example: <br>
     * {@code Recipe Editor - Brewing}
     *
     * @return The type of recipe this is for.
     */
    @NotNull
    String getRecipeTypeName();

    /**
     * @return The item that means this type of recipes.
     *         For example a workbench for shaped recipes,
     *         or a furnace for smelting.
     */
    @NotNull ItemStack getDisplayListItem();

    /**
     * Opens the correct recipe to the player.
     *
     * @param inv Edition Inventory by which the player is opening this
     * @param recipeName Name of the recipe
     * @param otherParams Whatever else required by the constructor of the {@link net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI}
     */
    void openForPlayer(@NotNull EditionInventory inv, @NotNull String recipeName, Object... otherParams);

    /**
     * This is the part that sends the recipe to mythiclib and what not.
     *
     * @param recipeTypeSection The configuration section [ID].base.crafting.[TYPE]
     *
     *                          You kind of have access to all other loaded recipes of this type,
     *                          not only the one being loaded, but please just load the one passed
     *                          as 'recipeName' parameter.
     *
     * @param recipeName Name of <u>the</u> recipe that is being loaded.
     *
     * @param namespace Namespace under which you should save this recipe.
     *
     *                  It will initially have the Namespaced Key you should use, but
     *                  when you pass it to MythicLib, MythicLib will make it null if
     *                  the recipe fails to register onto the crafting book, which is
     *                  the expected behaviour.
     *
     * @return The Activated Recipe Blueprint (so that it can be unloaded when reloading recipes)
     *
     * @throws IllegalArgumentException If anything goes wrong. THIS MEANS THE RECIPE WAS NOT ENABLED.
     *
     */
    @NotNull MythicRecipeBlueprint sendToMythicLib(@NotNull MMOItemTemplate template, @NotNull ConfigurationSection recipeTypeSection, @NotNull String recipeName, @NotNull Ref<NamespacedKey> namespace, @NotNull FriendlyFeedbackProvider ffp) throws IllegalArgumentException;
}
