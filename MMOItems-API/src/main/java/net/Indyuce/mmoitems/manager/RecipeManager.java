package net.Indyuce.mmoitems.manager;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipeBlueprint;
import io.lumine.mythic.lib.api.crafting.uifilters.VanillaUIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.api.util.Ref;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackMessage;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.crafting.MMOItemUIFilter;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.AirIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.MMOItemIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.VanillaIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.WorkbenchIngredient;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.recipe.RecipeBrowserGUI;
import net.Indyuce.mmoitems.gui.edition.recipe.gui.RecipeMakerGUI;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.RecipeRegistry;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.burninglegacy.BurningRecipeInformation;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Manages the custom crafting of MMOItem components and stuff.
 *
 * @author Aria, Gunging
 */
public class RecipeManager implements Reloadable {

    /**
     * Recipes which are handled by the vanilla spigot API.
     * All recipes registered here are Keyed
     */
    private final ArrayList<Recipe> loadedLegacyRecipes = new ArrayList<>();

    /**
     * Recipes handled by MMOItems and MythicLib.
     * They do support the knowledge book.
     */
    private final ArrayList<MythicRecipeBlueprint> customRecipes = new ArrayList<>();

    /**
     * Recipes handled by MMOItems but not MythicLib.
     * They do not support the knowledge book.
     */
    private final ArrayList<MythicRecipeBlueprint> booklessRecipes = new ArrayList<>();

    @NotNull
    private final ArrayList<NamespacedKey> blacklistedFromAutomaticDiscovery = new ArrayList<>();

    private boolean book;

    public void loadRecipes() {
        this.book = MMOItems.plugin.getConfig().getBoolean("recipes.use-recipe-book");

        // For logging
        FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());
        ffp.activatePrefix(true, "Custom Crafting");

        // Every single type yes
        for (Type type : MMOItems.plugin.getTypes().getAll()) {

            // Find their config
            FileConfiguration config = type.getConfigFile().getConfig();

            // For every template of those types
            for (MMOItemTemplate template : MMOItems.plugin.getTemplates().getTemplates(type)) {

                // Does it have a crafting recipe?
                if (config.contains(template.getId() + ".base.crafting")) {

                    // Get section containing the crafting recipes
                    ConfigurationSection section = RecipeMakerGUI.getSection(config, template.getId() + ".base.crafting");

                    // All loaded recipes
                    for (String recipeType : RecipeBrowserGUI.getRegisteredRecipes()) {

                        // Is it in-yo?
                        if (section.contains(recipeType)) {

                            // Get Registry
                            RecipeRegistry rr = RecipeBrowserGUI.getRegisteredRecipe(recipeType);

                            // Get recipe type section
                            ConfigurationSection typeSection = RecipeMakerGUI.getSection(section, recipeType);

                            // Register dem
                            for (String recipeName : typeSection.getKeys(false)) {

                                // Generate its key
                                NamespacedKey nk = getRecipeKey(template.getType(), template.getId(), recipeType, recipeName);

                                // Wrap
                                Ref<NamespacedKey> nkRef = new Ref<>(nk);

                                // Error yes
                                FriendlyFeedbackProvider ffpMinor = new FriendlyFeedbackProvider(FFPMMOItems.get());
                                ffpMinor.activatePrefix(true, "Recipe of $u" + template.getType() + " " + template.getId());

                                // Send to mythiclib
                                try {

                                    // The result of sending to MythicLib
                                    MythicRecipeBlueprint blueprint = rr.sendToMythicLib(template, typeSection, recipeName, nkRef, ffpMinor);
                                    (nkRef.getValue() != null ? customRecipes : booklessRecipes).add(blueprint);

                                    // Well something went wrong...
                                } catch (IllegalArgumentException error) {

                                    // Empty message? Snooze that
                                    if (!error.getMessage().isEmpty()) {

                                        // Log error
                                        MMOItems.print(null, "Cannot register custom recipe '$u{2}$b' for $e{0} {1}$b;$f {3}", "Custom Crafting", type.getId(), template.getId(), recipeName, error.getMessage());

                                        // Include failures in the report
                                        ffpMinor.sendTo(FriendlyFeedbackCategory.ERROR, MMOItems.getConsole());
                                        ffpMinor.sendTo(FriendlyFeedbackCategory.FAILURE, MMOItems.getConsole());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Log relevant messages
        ffp.sendTo(FriendlyFeedbackCategory.ERROR, MMOItems.getConsole());
        ffp.sendTo(FriendlyFeedbackCategory.FAILURE, MMOItems.getConsole());

        // Load legacy recipes onto Bukkit System
        Bukkit.getScheduler().runTask(MMOItems.plugin, () -> getBukkitRecipes().forEach(Bukkit::addRecipe));
    }

    public void registerBurningRecipe(@NotNull BurningRecipeType recipeType, @NotNull MMOItem mmo, @NotNull BurningRecipeInformation info, int amount, @NotNull NamespacedKey key, boolean hidden) {

        // Build its item stacc
        ItemStack stack = mmo.newBuilder().build();
        stack.setAmount(amount);

        // Do whatever this is / I just wont touch it
        CookingRecipe<?> recipe = recipeType.provideRecipe(key, stack, info.getChoice().toBukkit(), info.getExp(), info.getBurnTime());

        // Register that recipe lets goo
        loadedLegacyRecipes.add(recipe);

        if (hidden) {
            blacklistedFromAutomaticDiscovery.add(key);
        }
    }

    @NotNull
    public ArrayList<Recipe> getBukkitRecipes() {
        return loadedLegacyRecipes;
    }

    @NotNull
    public ArrayList<MythicRecipeBlueprint> getCustomRecipes() {
        return customRecipes;
    }

    @NotNull
    public ArrayList<MythicRecipeBlueprint> getBooklessRecipes() {
        return booklessRecipes;
    }

    @Nullable
    private ArrayList<NamespacedKey> generatedNamespacedKeys;

    public ArrayList<NamespacedKey> getNamespacedKeys() {
        if (generatedNamespacedKeys != null)
            return generatedNamespacedKeys;

        // Collect all name-spaced keys
        generatedNamespacedKeys = new ArrayList<>();
        customRecipes.forEach(blueprint -> generatedNamespacedKeys.add(blueprint.getNk()));
        loadedLegacyRecipes.forEach(recipe -> generatedNamespacedKeys.add(((Keyed) recipe).getKey()));

        return generatedNamespacedKeys;
    }

    @NotNull
    public NamespacedKey getRecipeKey(@NotNull Type type, @NotNull String id, @NotNull String recipeType, @NotNull String number) {
        return new NamespacedKey(MMOItems.plugin, recipeType + "_" + type.getId() + "_" + id + "_" + number);
    }

    /**
     * Unregisters bukkit and MythicLib recipes and loads everything again.
     */
    public void reload() {
        Bukkit.getScheduler().runTask(MMOItems.plugin, () -> {

            // Remove all recipes
            for (NamespacedKey recipe : getNamespacedKeys()) {
                if (recipe == null) {
                    continue;
                }
                try {
                    Bukkit.removeRecipe(recipe);
                } catch (Throwable e) {
                    MMOItems.print(null, "Could not register crafting book recipe for $r{0}$b:$f {1}", "MMOItems Custom Crafting", recipe.getKey(), e.getMessage());
                }
            }

            // Clear loaded recipes
            loadedLegacyRecipes.clear();
            blacklistedFromAutomaticDiscovery.clear();

            // Disable and forget all blueprints
            customRecipes.forEach(blueprint -> {
                try {
                    blueprint.disable();
                    Bukkit.removeRecipe(blueprint.getNk());
                } catch (Throwable throwable) {
                    MMOItems.plugin.getLogger().log(Level.INFO, "Could not unregister knowledge book recipe '" + blueprint.getNk() + "': " + throwable.getMessage());
                }
            });
            customRecipes.clear();

            for (MythicRecipeBlueprint b : booklessRecipes)
                b.disable();
            booklessRecipes.clear();

            // Load all recipes
            generatedNamespacedKeys = null;
            loadRecipes();

            // Refresh book for online players
            if (book)
                for (Player player : Bukkit.getOnlinePlayers())
                    refreshRecipeBook(player);
        });
    }

    /**
     * TODO For some reason, we have to refresh the book every time
     * the player joins the server or something; the thing is
     * that recipes that are hidden from the book are lost when
     * doing this (if they had unlocked them somehow).
     * -
     * Kind of need to somehow remember what recipes have been
     * unlocked by who so that they don't get lost...
     */
    public void refreshRecipeBook(Player player) {

        // Book disabled? Hide all recipes
        if (!book) {
            for (NamespacedKey key : player.getDiscoveredRecipes())
                if ("mmoitems".equals(key.getNamespace()))
                    player.undiscoverRecipe(key);

            return;
        }

        if (MythicLib.plugin.getVersion().isStrictlyHigher(1, 16)) {

            // Undiscovers the recipes apparently
            for (NamespacedKey key : player.getDiscoveredRecipes())
                if ("mmoitems".equals(key.getNamespace()) && !getNamespacedKeys().contains(key))
                    player.undiscoverRecipe(key);

            // And discovers them again
            for (NamespacedKey recipe : getNamespacedKeys()) {
                if (recipe == null)
                    continue;

                // Not blacklisted right
                boolean blacklisted = false;
                for (NamespacedKey black : blacklistedFromAutomaticDiscovery) {

                    if (recipe.equals(black)) {
                        blacklisted = true;
                        break;
                    }
                }
                if (blacklisted) {
                    continue;
                }

                try {
                    if (!player.hasDiscoveredRecipe(recipe)) {
                        player.discoverRecipe(recipe);
                    }
                } catch (Throwable e) {
                    MMOItems.print(null, "Could not register crafting book recipe for $r{0}$b:$f {1}", "MMOItems Custom Crafting", recipe.getKey(), e.getMessage());
                }
            }

            // Done woah
            return;
        }

        // Discovers all recipes
        for (NamespacedKey recipe : getNamespacedKeys()) {
            if (recipe == null) {
                continue;
            }

            // Not blacklisted aight
            boolean blacklisted = false;
            for (NamespacedKey black : blacklistedFromAutomaticDiscovery) {

                if (recipe.equals(black)) {
                    blacklisted = true;
                    break;
                }
            }
            if (blacklisted) {
                continue;
            }

            try {
                player.discoverRecipe(recipe);
            } catch (Throwable e) {
                MMOItems.print(null, "Could not register crafting book recipe for $r{0}$b:$f {1}", "MMOItems Custom Crafting", recipe.getKey(), e.getMessage());
            }
        }
    }

    @NotNull
    public static WorkbenchIngredient getWorkbenchIngredient(@NotNull String input) throws IllegalArgumentException {

        // Read it this other way ~
        ProvidedUIFilter poof = ProvidedUIFilter.getFromString(RecipeMakerGUI.poofFromLegacy(input), null);

        // Air is AIR
        if (poof == null) {
            return new AirIngredient();
        }

        // With class, obviously - no need for prefix tho
        FriendlyFeedbackProvider ffp = new FriendlyFeedbackProvider(FFPMMOItems.get());

        // Valid right
        if (!poof.isValid(ffp)) {

            // Snooze that
            //noinspection ConstantConditions
            throw new IllegalArgumentException(SilentNumbers.collapseList(SilentNumbers.transcribeList(ffp.getFeedbackOf(FriendlyFeedbackCategory.ERROR), s -> ((FriendlyFeedbackMessage) s).forConsole(FFPMMOItems.get())), ". "));
        }

        // Get amount
        int amount = poof.getAmount(0);

        // MMOItem?
        if (poof.getParent() instanceof MMOItemUIFilter) {

            // Get those
            Type miType = MMOItems.plugin.getTypes().getOrThrow(poof.getArgument());

            // Find template
            MMOItemTemplate mmo = MMOItems.plugin.getTemplates().getTemplateOrThrow(miType, poof.getData());

            // Treat is as MMOItem :pogyoo:
            return new MMOItemIngredient(miType, mmo.getId(), amount);

            // Must be vanilla
        } else if (poof.getParent() instanceof VanillaUIFilter) {

            return new VanillaIngredient(Material.valueOf(poof.getArgument().toUpperCase().replace("-", "_").replace(" ", "_")), amount);
        }

        throw new IllegalArgumentException("Unsupported ingredient, you may only specify vanilla or mmoitems.");
    }

    /**
     * Easier control of furnace, smoker, campfire and blast recipes so there is
     * no need to have four time the same method to register this type of recipe
     *
     * @author cympe
     */
    public enum BurningRecipeType {
        FURNACE(FurnaceRecipe::new),
        SMOKER(SmokingRecipe::new),
        CAMPFIRE(CampfireRecipe::new),
        BLAST(BlastingRecipe::new);

        private final RecipeProvider provider;

        BurningRecipeType(RecipeProvider provider) {
            this.provider = provider;
        }

        public CookingRecipe<?> provideRecipe(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookTime) {
            return provider.provide(key, result, source, experience, cookTime);
        }

        public String getPath() {
            return name().toLowerCase();
        }
    }

    @FunctionalInterface
    public interface RecipeProvider {
        CookingRecipe<?> provide(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookTime);
    }
}
