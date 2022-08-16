package net.Indyuce.mmoitems.manager;

import java.util.*;
import java.util.stream.Collectors;

import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipeBlueprint;
import io.lumine.mythic.lib.api.crafting.uifilters.VanillaUIFilter;
import io.lumine.mythic.lib.api.crafting.uimanager.ProvidedUIFilter;
import io.lumine.mythic.lib.api.util.Ref;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackMessage;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import io.lumine.mythic.lib.api.util.ui.SilentNumbers;
import net.Indyuce.mmoitems.api.crafting.MMOItemUIFilter;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import net.Indyuce.mmoitems.gui.edition.recipe.RecipeBrowserGUI;
import net.Indyuce.mmoitems.gui.edition.recipe.recipes.RecipeMakerGUI;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.RecipeRegistry;
import net.Indyuce.mmoitems.gui.edition.recipe.registry.burninglegacy.BurningRecipeInformation;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmokingRecipe;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.recipe.workbench.CustomRecipe;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.AirIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.MMOItemIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.VanillaIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.WorkbenchIngredient;
import io.lumine.mythic.lib.MythicLib;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages the custom crafting of MMOItem components and stuff.
 *
 * @author Aria, Gunging
 */
public class RecipeManager implements Reloadable {

	/**
	 * Custom recipes which are handled by MMOItems
	 */
	final HashSet<CustomRecipe> legacyCraftingRecipes = new HashSet<>();
	/**
	 * Recipes which are handled by the vanilla spigot API. All recipes
	 * registered here are Keyed
	 */
	final HashSet<Recipe> loadedLegacyRecipes = new HashSet<>();

	/**
	 * All the custom recipes loaded by MMOItems.
	 * <p></p>
	 * <b>Except that for the time being, only Workbench recipes are supported
	 * by mythic lib so for any other kind use the legacy array.</b>
	 */
	final HashMap<NamespacedKey, MythicRecipeBlueprint> customRecipes = new HashMap<>();
	final ArrayList<MythicRecipeBlueprint> booklessRecipes = new ArrayList<>();
	@NotNull ArrayList<NamespacedKey> blacklistedFromAutomaticDiscovery = new ArrayList<>();

	private boolean book, amounts;

	/**
	 * @param book    Vanilla knowledge book support.
	 *
	 * @param amounts If the recipe system should support glitchy multi amount
	 *                recipes. Ignored by MythicLib recipes.
	 */
	public void load(boolean book, boolean amounts) {
		this.book = book;
		this.amounts = amounts; }

	public boolean isAmounts() { return amounts; }

	public void loadRecipes() {
		legacyCraftingRecipes.clear();

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

									// Was it registered in the book, then?
									if (nkRef.getValue() != null) {
										customRecipes.put(nkRef.getValue(), blueprint);

										// Bookless, include in the other list.
									} else { booklessRecipes.add(blueprint); }


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

		// Sort recipes
		sortRecipes();

		// Load legacy recipes onto Bukkit System
		Bukkit.getScheduler().runTask(MMOItems.plugin, () -> getLoadedLegacyRecipes().forEach(Bukkit::addRecipe));
	}

	public void registerBurningRecipe(@NotNull BurningRecipeType recipeType, @NotNull MMOItem mmo, @NotNull BurningRecipeInformation info, int amount, @NotNull NamespacedKey key, boolean hidden) {

		// Build its item stacc
		ItemStack stack = mmo.newBuilder().build();
		stack.setAmount(amount);

		// Do whatever this is / I just wont touch it
		CookingRecipe<?> recipe = recipeType.provideRecipe(key, stack, info.getChoice().toBukkit(), info.getExp(), info.getBurnTime());

		// Register that recipe lets goo
		loadedLegacyRecipes.add(recipe);

		if (hidden) { blacklistedFromAutomaticDiscovery.add(key); }
	}

	public void registerRecipeAsCustom(CustomRecipe recipe) {
		if (!recipe.isEmpty())
			legacyCraftingRecipes.add(recipe);
	}

	public void registerRecipeAsBukkit(CustomRecipe recipe, String number) {
		NamespacedKey key = getRecipeKey(recipe.getType(), recipe.getId(), recipe.isShapeless() ? "shapeless" : "shaped", number);
		if (!recipe.isEmpty())
			loadedLegacyRecipes.add(recipe.asBukkit(key));
	}

	public Set<Recipe> getLoadedLegacyRecipes() {
		return loadedLegacyRecipes;
	}

	public Set<CustomRecipe> getLegacyCustomRecipes() {
		return legacyCraftingRecipes;
	}
	public HashMap<NamespacedKey, MythicRecipeBlueprint> getCustomRecipes() { return customRecipes; }
	public ArrayList<MythicRecipeBlueprint> getBooklessRecipes() { return booklessRecipes; }

	@Nullable
	ArrayList<NamespacedKey> generatedNKs;
	public ArrayList<NamespacedKey> getNamespacedKeys() {

		if (generatedNKs != null) { return generatedNKs; }

		// Collect all Namespaces
		ArrayList<NamespacedKey> nkMythic = new ArrayList<>(customRecipes.keySet());
		ArrayList<NamespacedKey> nkLegacy = loadedLegacyRecipes.stream().map(recipe -> ((Keyed) recipe).getKey()).distinct().collect(Collectors.toCollection(ArrayList::new));
		nkMythic.addAll(nkLegacy);
		generatedNKs = new ArrayList<>();
		for (NamespacedKey nk : nkMythic) { if (nk != null) { generatedNKs.add(nk); }}
		return generatedNKs;
	}

	public void sortRecipes() {
		List<CustomRecipe> temporary = new ArrayList<>(legacyCraftingRecipes);
		legacyCraftingRecipes.clear();
		legacyCraftingRecipes.addAll(temporary.stream().sorted().collect(Collectors.toList()));
	}

	@NotNull public NamespacedKey getRecipeKey(@NotNull Type type, @NotNull String id, @NotNull String recipeType, @NotNull String number) {
		return new NamespacedKey(MMOItems.plugin, recipeType + "_" + type.getId() + "_" + id + "_" + number);
	}

	/**
	 * Unregisters bukkit and MythicLib recipes and loads everything again.
	 */
	public void reload() {
		Bukkit.getScheduler().runTask(MMOItems.plugin, () -> {

			// Remove all recipes
			for (NamespacedKey recipe : getNamespacedKeys()) {
				if (recipe == null) { continue; }
				try { Bukkit.removeRecipe(recipe); }
				catch (Throwable e) { MMOItems.print(null, "Could not register crafting book recipe for $r{0}$b:$f {1}", "MMOItems Custom Crafting", recipe.getKey(), e.getMessage()); }
			}

			// Clear loaded recipes
			loadedLegacyRecipes.clear();
			blacklistedFromAutomaticDiscovery.clear();

			// Disable and forget all blueprints
			for (NamespacedKey b : customRecipes.keySet()) {
				if (b == null) { continue; }
				customRecipes.get(b).disable();
				try { Bukkit.removeRecipe(b); }

				catch (Throwable e) { MMOItems.print(null, "Could not register crafting book recipe for $r{0}$b:$f {1}", "MMOItems Custom Crafting", b.getKey(), e.getMessage()); } }
			customRecipes.clear();

			for (MythicRecipeBlueprint b : booklessRecipes) { b.disable(); }
			booklessRecipes.clear();

			// Load all recipes
			generatedNKs = null;
			loadRecipes();

			// Refresh the book I suppose
			if (book) { for (Player player : Bukkit.getOnlinePlayers()) { refreshRecipeBook(player); } }

		});
	}

	public void refreshRecipeBook(Player player) {

		/*
		 * todo For some reason, we have to refresh the book every time
		 *      the player joins the server or something; the thing is
		 *      that recipes that are hidden from the book are lost when
		 *      doing this (if they had unlocked them somehow).
		 *      -
		 *      Kind of need to somehow remember what recipes have been
		 *      unlocked by who so that they don't get lost...
		 */

		// Book disabled?
		if (!book) {

			// Hide all recipes
			for (NamespacedKey key : player.getDiscoveredRecipes()) { if ("mmoitems".equals(key.getNamespace())) { player.undiscoverRecipe(key); } }

			// Done woah
			return;
		}


		if (MythicLib.plugin.getVersion().isStrictlyHigher(1, 16)) {

			// Undiscovers the recipes apparently
			for (NamespacedKey key : player.getDiscoveredRecipes()) {
				if ("mmoitems".equals(key.getNamespace()) && !getNamespacedKeys().contains(key)) { player.undiscoverRecipe(key); } }

			// And discovers them again
			for (NamespacedKey recipe : getNamespacedKeys()) {
				if (recipe == null) { continue; }

				// Not blacklisted right
				boolean blacklisted = false;
				for (NamespacedKey black : blacklistedFromAutomaticDiscovery) {

					if (recipe.equals(black)) { blacklisted = true; break; } }
				if (blacklisted) { continue; }

				try { if (!player.hasDiscoveredRecipe(recipe)) { player.discoverRecipe(recipe); } }
				catch (Throwable e) { MMOItems.print(null, "Could not register crafting book recipe for $r{0}$b:$f {1}", "MMOItems Custom Crafting", recipe.getKey(), e.getMessage()); }
			}

			// Done woah
			return;
		}

		// Discovers all recipes
		for (NamespacedKey recipe : getNamespacedKeys()) {
			if (recipe == null) { continue; }

			// Not blacklisted aight
			boolean blacklisted = false;
			for (NamespacedKey black : blacklistedFromAutomaticDiscovery) {

				if (recipe.equals(black)) { blacklisted = true; break; } }
			if (blacklisted) { continue; }

			try { player.discoverRecipe(recipe); }
			catch (Throwable e) { MMOItems.print(null, "Could not register crafting book recipe for $r{0}$b:$f {1}", "MMOItems Custom Crafting", recipe.getKey(), e.getMessage()); }
		}
	}

	@NotNull public static WorkbenchIngredient getWorkbenchIngredient(@NotNull String input) throws IllegalArgumentException {

		// Read it this other way ~
		ProvidedUIFilter poof = ProvidedUIFilter.getFromString(RecipeMakerGUI.poofFromLegacy(input), null);

		// Air is AIR
		if (poof == null) { return new AirIngredient(); }

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
	public interface RecipeProvider { CookingRecipe<?> provide(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookTime);}
}
