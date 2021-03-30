package net.Indyuce.mmoitems.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipeBlueprint;
import io.lumine.mythic.lib.api.crafting.recipes.MythicRecipeStation;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackCategory;
import io.lumine.mythic.lib.api.util.ui.FriendlyFeedbackProvider;
import net.Indyuce.mmoitems.api.crafting.recipe.SmithingCombinationType;
import net.Indyuce.mmoitems.api.util.message.FFPMMOItems;
import org.apache.commons.lang.Validate;
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

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.recipe.workbench.CustomRecipe;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.AirIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.MMOItemIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.VanillaIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.WorkbenchIngredient;
import net.Indyuce.mmoitems.stat.data.DoubleData;
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
	final HashSet<MythicRecipeBlueprint> customRecipes = new HashSet<>();

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

					try {

						ConfigurationSection section = config.getConfigurationSection(template.getId() + ".base.crafting");

						// Delegate recipes to their parsers
						if (section.contains("shaped"))
							section.getConfigurationSection("shaped").getKeys(false).forEach(
									recipe -> registerRecipe(type, template.getId(), section.getStringList("shaped." + recipe), false, recipe));
						if (section.contains("shapeless"))
							section.getConfigurationSection("shapeless").getKeys(false).forEach(
									recipe -> registerRecipe(type, template.getId(), section.getStringList("shapeless." + recipe), true, recipe));
						if (section.contains("furnace"))
							section.getConfigurationSection("furnace").getKeys(false)
									.forEach(recipe -> registerBurningRecipe(BurningRecipeType.FURNACE, type, template.getId(),
											new BurningRecipeInformation(section.getConfigurationSection("furnace." + recipe)), recipe));
						if (section.contains("blast"))
							section.getConfigurationSection("blast").getKeys(false)
									.forEach(recipe -> registerBurningRecipe(BurningRecipeType.BLAST, type, template.getId(),
											new BurningRecipeInformation(section.getConfigurationSection("blast." + recipe)), recipe));
						if (section.contains("smoker"))
							section.getConfigurationSection("smoker").getKeys(false)
									.forEach(recipe -> registerBurningRecipe(BurningRecipeType.SMOKER, type, template.getId(),
											new BurningRecipeInformation(section.getConfigurationSection("smoker." + recipe)), recipe));
						if (section.contains("campfire"))
							section.getConfigurationSection("campfire").getKeys(false)
									.forEach(recipe -> registerBurningRecipe(BurningRecipeType.CAMPFIRE, type, template.getId(),
											new BurningRecipeInformation(section.getConfigurationSection("campfire." + recipe)), recipe));
						if (section.contains("smithing"))
							section.getConfigurationSection("smithing").getKeys(false).forEach(recipe -> registerSmithingRecipe(type,
									template.getId(), section.getConfigurationSection("smithing." + recipe), recipe));

					// Uh heck
					} catch (IllegalArgumentException exception) {

						// Add message
						ffp.log(FriendlyFeedbackCategory.ERROR, "Could not load recipe of $f{0} {1}$b: {2}",
								type.getId(), template.getId(), exception.getMessage());
					}
				}
			}
		}

		// Log all
		ffp.sendAllTo(MMOItems.getConsole());

		// Sort recipes
		sortRecipes();

		// Load legacy recipes onto Bukkit System
		Bukkit.getScheduler().runTask(MMOItems.plugin, () -> getLoadedLegacyRecipes().forEach(Bukkit::addRecipe));
	}

	public void registerBurningRecipe(BurningRecipeType recipeType, Type type, String id, BurningRecipeInformation info, String recipeId) {
		NamespacedKey key = getRecipeKey(type, id, recipeType.getPath(), recipeId);
		MMOItem mmo = MMOItems.plugin.getMMOItem(type, id);
		final int amount = mmo.hasData(ItemStats.CRAFT_AMOUNT) ? (int) ((DoubleData) mmo.getData(ItemStats.CRAFT_AMOUNT)).getValue() : 1;
		ItemStack stack = mmo.newBuilder().build();
		stack.setAmount(amount);
		CookingRecipe<?> recipe = recipeType.provideRecipe(key, stack, info.getChoice().toBukkit(), info.getExp(), info.getBurnTime());
		loadedLegacyRecipes.add(recipe);
	}

	public void registerSmithingRecipe(@NotNull Type type, @NotNull String id, @NotNull ConfigurationSection section, @NotNull String number) throws IllegalArgumentException {
		Validate.isTrue(section.isString("input1") && section.isString("input2"), "Invalid smithing recipe for '" + type.getId() + " . " + id + "'");

		String item = section.getString("input1");
		String ingot = section.getString("input2");
		boolean dropGems = section.getBoolean("drop-gems", false);
		String upgrade = section.getString("upgrades" );
		String enchants = section.getString("enchantments" );
		if (item == null) { item = ""; }
		if (ingot == null) { ingot = ""; }
		if (upgrade == null) { upgrade = SmithingCombinationType.MAXIMUM.toString(); }
		if (enchants == null) { enchants = SmithingCombinationType.MAXIMUM.toString(); }

		MythicRecipeBlueprint blueprint = CustomRecipe.generateSmithing(type, id, item, ingot, dropGems, enchants, upgrade);

		// Remember it
		customRecipes.add(blueprint);

		// Enable it
		blueprint.deploy(MythicRecipeStation.SMITHING);
	}

	/**
	 * Parses a shapeless or shaped workbench crafting recipe and registers it.
	 * 
	 * @param type      The item type
	 * @param id        The item ID
	 * @param list      The list of items (3 lines or 3 ingredients, separated
	 *                  by spaces)
	 * @param shapeless If the recipe is shapeless or not
	 * @param recipeID  Every item can have multiple recipe, there's one number
	 *                  per recipe to differenciate them
	 */
	public void registerRecipe(@NotNull Type type, @NotNull String id, @NotNull List<String> list, boolean shapeless, @Nullable String recipeID) throws IllegalArgumentException {

		/*
		 * The output of the recipe will be the MMOItem of this Type and ID which
		 * is guaranteed to be loaded.
		 *
		 * The input is defined in the list in the following formats:
		 *
		 * SHAPELESS:
		 *  + A list of 9 entries, which can be in any order
		 *  + Each entry is one item, may be vanilla, MMOItem, or UIFilter.
		 *
		 * SHAPED
		 *  + A list of 3 entries, which are in order.
		 *  + Each entry is 3 items, separated by spaces, except if UIFilters are used,
		 *    which can cause more than 3 items to be apparent.
		 *    * Logic to parse UIFilters is included.
		 *  + They indicate the rows of the crafting table.
		 */

		MythicRecipeBlueprint blueprint;
		if (shapeless) {

			// Generate with no shape
			blueprint = CustomRecipe.generateShapeless(type, id, list);
		} else {

			// Generate shaped
			blueprint = CustomRecipe.generateShaped(type, id, list);
		}

		// Remember it
		customRecipes.add(blueprint);

		// Enable it
		blueprint.deploy(MythicRecipeStation.WORKBENCH);

		/*
		CustomRecipe recipe = new CustomRecipe(type, id, list, shapeless);

		if (amounts)
			registerRecipeAsCustom(recipe);
		  else
			registerRecipeAsBukkit(recipe, number);
	    */
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
	public HashSet<MythicRecipeBlueprint> getCustomRecipes() { return customRecipes; }

	public Set<NamespacedKey> getNamespacedKeys() {
		return loadedLegacyRecipes.stream().map(recipe -> ((Keyed) recipe).getKey()).collect(Collectors.toSet());
	}

	public void sortRecipes() {
		List<CustomRecipe> temporary = new ArrayList<>(legacyCraftingRecipes);
		legacyCraftingRecipes.clear();
		legacyCraftingRecipes.addAll(temporary.stream().sorted().collect(Collectors.toList()));
	}

	public NamespacedKey getRecipeKey(Type type, String id, String recipeType, String number) {
		return new NamespacedKey(MMOItems.plugin, recipeType + "_" + type.getId() + "_" + id + "_" + number);
	}

	/**
	 * Unregisters bukkit and MythicLib recipes and loads everything again.
	 */
	public void reload() {
		Bukkit.getScheduler().runTask(MMOItems.plugin, () -> {

			// Remove all recipes
			for (NamespacedKey recipe : getNamespacedKeys()) { Bukkit.removeRecipe(recipe); }

			// Clear loaded recipes
			loadedLegacyRecipes.clear();

			// Disable and forget all blueprints
			for (MythicRecipeBlueprint b : customRecipes) { b.disable(); }
			customRecipes.clear();

			// Load all recipes
			loadRecipes();

			// Refresh the book I suppose
			if (book) { for (Player player : Bukkit.getOnlinePlayers()) { refreshRecipeBook(player); } }

		});
	}

	public void refreshRecipeBook(Player player) {

		// Book disabled?
		if (!book) {

			// Hide all recipes
			for (NamespacedKey key : player.getDiscoveredRecipes()) { if (key.getNamespace().equals("mmoitems")) { player.undiscoverRecipe(key); } }

			// Done woah
			return;
		}


		if (MythicLib.plugin.getVersion().isStrictlyHigher(1, 16)) {

			// Undiscovers the recipes apparently
			for (NamespacedKey key : player.getDiscoveredRecipes()) { if (key.getNamespace().equals("mmoitems") && !getNamespacedKeys().contains(key)) { player.undiscoverRecipe(key); } }

			// And discovers them again, sweet!
			for (NamespacedKey recipe : getNamespacedKeys()) { if (!player.hasDiscoveredRecipe(recipe)) { player.discoverRecipe(recipe); } }

			// Done woah
			return;
		}

		// Discovers all recipes
		for (NamespacedKey recipe : getNamespacedKeys()) { player.discoverRecipe(recipe); }
	}

	public WorkbenchIngredient getWorkbenchIngredient(String input) {
		String[] split = input.split(":");
		int amount = split.length > 1 ? Integer.parseInt(split[1]) : 1;

		if (split[0].contains(".")) {
			String[] split1 = split[0].split("\\.");
			Type type = MMOItems.plugin.getTypes().getOrThrow(split1[0].toUpperCase().replace("-", "_").replace(" ", "_"));
			MMOItemTemplate template = MMOItems.plugin.getTemplates().getTemplateOrThrow(type,
					split1[1].toUpperCase().replace("-", "_").replace(" ", "_"));
			return new MMOItemIngredient(type, template.getId(), amount);
		}

		if (split[0].equalsIgnoreCase("air"))
			return new AirIngredient();

		return new VanillaIngredient(Material.valueOf(split[0].toUpperCase().replace("-", "_").replace(" ", "_")), amount);
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

	/**
	 * Used to handle furnace/smoker/campfire/furnace extra crafting recipe
	 * parameters
	 * 
	 * @author ASangarin
	 */
	public class BurningRecipeInformation {
		private final WorkbenchIngredient choice;
		private final float exp;
		private final int burnTime;

		public BurningRecipeInformation(ConfigurationSection config) {
			choice = getWorkbenchIngredient(config.getString("item"));
			exp = (float) config.getDouble("exp", 0.35);
			burnTime = config.getInt("time", 200);
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
}
