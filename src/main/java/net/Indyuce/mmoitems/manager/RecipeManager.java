package net.Indyuce.mmoitems.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmokingRecipe;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.item.template.MMOItemTemplate;
import net.Indyuce.mmoitems.api.recipe.workbench.CustomRecipe;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.AirIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.MMOItemIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.VanillaIngredient;
import net.Indyuce.mmoitems.api.recipe.workbench.ingredients.WorkbenchIngredient;

public class RecipeManager {

	/**
	 * Custom recipes which are handled by MMOItems
	 */
	private final Set<CustomRecipe> craftingRecipes = new HashSet<>();

	/**
	 * Recipes which are handled by the vanilla spigot API.
	 * All recipes registered here are Keyed
	 */
	private final Set<Recipe> loadedRecipes = new HashSet<>();

	public void loadRecipes() {
		craftingRecipes.clear();

		for (Type type : MMOItems.plugin.getTypes().getAll()) {
			FileConfiguration config = type.getConfigFile().getConfig();
			for (MMOItemTemplate template : MMOItems.plugin.getTemplates().getTemplates(type))
				if (config.contains(template.getId() + ".base.crafting"))
					try {
						ConfigurationSection section = config.getConfigurationSection(template.getId() + ".base.crafting");

						if (section.contains("shaped"))
							section.getConfigurationSection("shaped").getKeys(false)
									.forEach(recipe -> registerShapedRecipe(type, template.getId(), section.getStringList("shaped." + recipe)));
						if (section.contains("shapeless"))
							section.getConfigurationSection("shapeless").getKeys(false)
									.forEach(recipe -> registerShapelessRecipe(type, template.getId(), section.getStringList("shapeless." + recipe)));
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
					} catch (IllegalArgumentException exception) {
						MMOItems.plugin.getLogger().log(Level.WARNING,
								"Could not load recipe of '" + template.getId() + "': " + exception.getMessage());
					}
		}

		sortRecipes();
		Bukkit.getScheduler().runTask(MMOItems.plugin, () -> getLoadedRecipes().forEach(recipe -> Bukkit.addRecipe(recipe)));
	}

	public void registerBurningRecipe(BurningRecipeType recipeType, Type type, String id, BurningRecipeInformation info, String recipeId) {
		NamespacedKey key = getRecipeKey(type, id, recipeType.getPath(), recipeId);
		Recipe recipe = recipeType.provideRecipe(key, MMOItems.plugin.getItem(type, id), info.getChoice().toBukkit(), info.getExp(),
				info.getBurnTime());
		loadedRecipes.add(recipe);
	}

	public void registerShapedRecipe(Type type, String id, List<String> list) {
		registerRecipe(new CustomRecipe(type, id, list, false));
	}

	public void registerShapelessRecipe(Type type, String id, List<String> list) {
		registerRecipe(new CustomRecipe(type, id, list, true));
	}

	public void registerRecipe(CustomRecipe recipe) {
		if (!recipe.isEmpty())
			craftingRecipes.add(recipe);
	}

	public Set<Recipe> getLoadedRecipes() {
		return loadedRecipes;
	}

	public Set<CustomRecipe> getCustomRecipes() {
		return craftingRecipes;
	}

	public Set<NamespacedKey> getNamespacedKeys() {
		return loadedRecipes.stream().map(recipe -> ((Keyed) recipe).getKey()).collect(Collectors.toSet());
	}

	public void sortRecipes() {
		List<CustomRecipe> temporary = new ArrayList<>();
		temporary.addAll(craftingRecipes);
		craftingRecipes.clear();
		craftingRecipes.addAll(temporary.stream().sorted().collect(Collectors.toList()));
	}

	public NamespacedKey getRecipeKey(Type type, String id, String recipeType, String number) {
		return new NamespacedKey(MMOItems.plugin, recipeType + "_" + type.getId() + "_" + id + "_" + number);
	}

	/**
	 * Unregisters bukkit recipes and loads everything again
	 */
	public void reloadRecipes() {
		Bukkit.getScheduler().runTask(MMOItems.plugin, () -> {

			Iterator<Recipe> iterator = Bukkit.recipeIterator();
			while (iterator.hasNext()) {
				Recipe recipe = iterator.next();
				if (recipe instanceof Keyed && ((Keyed) recipe).getKey().getNamespace().equals("mmoitems"))
					iterator.remove();
			}

			loadedRecipes.clear();
			loadRecipes();
		});
	}

	public WorkbenchIngredient getWorkbenchIngredient(String input) {
		String[] split = input.split("\\:");
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
		FURNACE((key, result, source, experience, cookTime) -> new FurnaceRecipe(key, result, source, experience, cookTime)),
		SMOKER((key, result, source, experience, cookTime) -> new SmokingRecipe(key, result, source, experience, cookTime)),
		CAMPFIRE((key, result, source, experience, cookTime) -> new CampfireRecipe(key, result, source, experience, cookTime)),
		BLAST((key, result, source, experience, cookTime) -> new BlastingRecipe(key, result, source, experience, cookTime));

		private final RecipeProvider provider;

		private BurningRecipeType(RecipeProvider provider) {
			this.provider = provider;
		}

		public Recipe provideRecipe(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookTime) {
			return provider.provide(key, result, source, experience, cookTime);
		}

		public String getPath() {
			return name().toLowerCase();
		}
	}

	@FunctionalInterface
	public interface RecipeProvider {
		Recipe provide(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookTime);
	}

	/**
	 * Used to handle furnace/smoker/campifire/furnace extra crafting recipe
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
