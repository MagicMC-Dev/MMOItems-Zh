package net.Indyuce.mmoitems.gui.edition.recipe.registry;

import io.lumine.mythic.lib.api.crafting.ingredients.MythicRecipeIngredient;
import io.lumine.mythic.lib.api.crafting.outputs.MRORecipe;
import io.lumine.mythic.lib.api.crafting.outputs.MythicRecipeOutput;
import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipeBlueprint;
import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipeStation;
import io.lumine.mythic.lib.api.crafting.recipes.ShapedRecipe;
import io.lumine.mythic.lib.api.crafting.recipes.ShapelessRecipe;
import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.api.util.Ref;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.crafting.MMOItemUIFilter;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.gui.edition.recipe.rba.RBA_AmountOutput;
import net.Indyuce.mmoitems.gui.edition.recipe.rba.RBA_HideFromBook;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RMG_Shapeless;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.data.random.RandomStatData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RMGRR_Shapeless implements RecipeRegistry {

    @NotNull @Override public String getRecipeTypeName() { return "Shapeless"; }
    @NotNull @Override public String getRecipeConfigPath() { return "shapeless"; }

    @NotNull final ItemStack displayListItem = RecipeMakerGUI.rename(new ItemStack(Material.OAK_LOG), FFPMMOItems.get().getExampleFormat() + "Shapeless Recipe");
    @NotNull @Override public ItemStack getDisplayListItem() { return displayListItem; }

    @Override public void openForPlayer(@NotNull EditionInventory inv, @NotNull String recipeName, Object... otherParams) {
        new RMG_Shapeless(inv.getPlayer(), inv.getEdited(), recipeName, this).open(inv.getPreviousPage());
    }

    @NotNull
    @Override
    public MythicRecipeBlueprint sendToMythicLib(@NotNull MMOItemTemplate template, @NotNull ConfigurationSection recipeTypeSection, @NotNull String recipeName, @NotNull Ref<NamespacedKey> namespace, @NotNull FriendlyFeedbackProvider ffp) throws IllegalArgumentException {

        // Prior Preparations (update old formats)
        RecipeMakerGUI.moveInput(recipeTypeSection, recipeName);

        // Read some values
        ConfigurationSection recipeSection = RecipeMakerGUI.getSection(recipeTypeSection, recipeName);
        NamespacedKey nk = namespace.getValue();
        if (nk == null) { throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Illegal (Null) Namespace")); }

        //region Identify the input
        ArrayList<MythicRecipeIngredient> poofs = new ArrayList<>();
        ArrayList<String> recipe = new ArrayList<>(recipeSection.getStringList(RecipeMakerGUI.INPUT_INGREDIENTS));

        // Read from the recipe
        boolean nonAirFound = false;
        for (String str : recipe) {

            // Null is a sleeper
            if (str == null || "AIR".equals(str)) { continue; }

            // Add
            ProvidedUIFilter p = RecipeMakerGUI.readIngredientFrom(str, ffp);

            // Not air right
            if (p.isAir()) { continue; }

            // Ok snooze
            nonAirFound = true;
            poofs.add(new MythicRecipeIngredient(p));
        }
        if (!nonAirFound) { throw new IllegalArgumentException(FriendlyFeedbackProvider.quickForConsole(FFPMMOItems.get(), "Shapeless recipe containing only AIR, $fignored$b.")); }
        ShapelessRecipe input = new ShapelessRecipe(nk.getKey(), poofs);
        //endregion

        // Read the options and output
        ShapedRecipe output = RMGRR_Shaped.shapedRecipeFromList(nk.getKey(), new ArrayList<>(recipeSection.getStringList(RecipeMakerGUI.OUTPUT_INGREDIENTS)), ffp);
        int outputAmount = recipeSection.getInt(RBA_AmountOutput.AMOUNT_INGREDIENTS, 1);
        boolean hideBook = recipeSection.getBoolean(RBA_HideFromBook.BOOK_HIDDEN, false);

        // Build Output
        ShapedRecipe outputItem = ShapedRecipe.single(nk.getKey(),  new ProvidedUIFilter(MMOItemUIFilter.get(), template.getType().getId(), template.getId(), Math.max(outputAmount, 1)));
        MythicRecipeOutput outputRecipe = new MRORecipe(outputItem, output);

        // That's our blueprint :)
        MythicRecipeBlueprint ret = new MythicRecipeBlueprint(input, outputRecipe, nk);

        // Required permission?
        RandomStatData perm = template.getBaseItemData().get(ItemStats.CRAFT_PERMISSION);
        if (perm instanceof StringData) {

            // Ah yes
            String permission = ((StringData) perm).getString();

            // Finally
            if (permission != null) { ret.addRequiredPermission(permission); } }

        // Enable it
        ret.deploy(MythicRecipeStation.WORKBENCH, namespace);

        // Hide book if specified
        if (hideBook) { namespace.setValue(null); }

        // That's it
        return ret;
    }
}
