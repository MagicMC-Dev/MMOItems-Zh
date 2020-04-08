package net.Indyuce.mmoitems.manager.recipe;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.recipe.MMORecipeChoice;

public abstract class RecipeManager {

	/**
	 * TODO When Bukkit changes their 'RecipeChoice.ExactChoice' API we can
	 * remove the suppressed warnings, but right now it works despite being
	 * marked as deprecated. It is just a
	 */
	private final Set<LoadedRecipe> loadedRecipes = new HashSet<>();

	public abstract void reload();

	public abstract void registerFurnaceRecipe(Type type, String id, BurningRecipeInformation info, String number);

	public abstract void registerShapelessRecipe(Type type, String id, ConfigurationSection config, String number);

	public abstract void shapedIngredient(ShapedRecipe recipe, char slot, MMORecipeChoice choice);

	public abstract void registerShapedRecipe(Type type, String id, List<String> list, String number);

	public abstract void shapelessIngredient(ShapelessRecipe recipe, MMORecipeChoice choice);

	/**
	 * This method is purely for easily converting the AWB recipes.
	 * 
	 * @deprecated Some day I want to get proper rid of the AWB but right now we
	 *             don't want to force players to update their recipes right off
	 *             the bat.
	 */
	@Deprecated
	public abstract void setIngredientOrAir(ShapedRecipe recipe, char character, ConfigurationSection c);

	public void registerRecipe(NamespacedKey key, Recipe recipe) {
		loadedRecipes.add(new LoadedRecipe(key, recipe));
	}

	public Set<LoadedRecipe> getLoadedRecipes() {
		return loadedRecipes;
	}

	public Set<NamespacedKey> getNamespacedKeys() {
		return loadedRecipes.stream().map(recipe -> recipe.getKey()).collect(Collectors.toSet());
	}

	public NamespacedKey getRecipeKey(Type type, String id, String recipeType, String number) {
		return new NamespacedKey(MMOItems.plugin, recipeType + "_" + type.getId() + "_" + id + "_" + number);
	}

	public void reloadRecipes() {
		Bukkit.getScheduler().runTask(MMOItems.plugin, () -> {

			Iterator<Recipe> iterator = Bukkit.recipeIterator();
			while (iterator.hasNext()) {
				Recipe recipe = iterator.next();
				if (recipe instanceof Keyed && ((Keyed) recipe).getKey().getNamespace().equals("mmoitems"))
					iterator.remove();
			}

			loadedRecipes.clear();
			reload();
		});
	}

	/**
	 * @deprecated Some day I want to get proper rid of the AWB but right now we
	 *             don't want to force players to update their recipes right off
	 *             the bat.
	 */
	public void registerAdvancedWorkbenchRecipe(Type type, String id, FileConfiguration config) {
		MMOItems.plugin.getLogger().warning("Found deprecated adv. recipe for " + id + ". Converting it to the new system...");
		MMOItems.plugin.getLogger().warning("It is recommended to update your recipes!");

		NamespacedKey key = getRecipeKey(type, id, "advanced", "deprecated");
		ShapedRecipe recipe = new ShapedRecipe(key, MMOItems.plugin.getItems().getItem(type, id));
		recipe.shape("012", "345", "678");

		for (int j = 0; j < 9; j++) {
			ConfigurationSection section = config.getConfigurationSection(id + ".advanced-craft." + j);
			if (section != null)
				setIngredientOrAir(recipe, ("" + j).charAt(0), section);
		}

		registerRecipe(key, recipe);
	}

	/*
	 * used because spigot API does not let us access namespaced key of a Recipe
	 * instance.
	 */
	public class LoadedRecipe {
		private final Recipe recipe;
		private final NamespacedKey key;

		public LoadedRecipe(NamespacedKey key, Recipe recipe) {
			this.recipe = recipe;
			this.key = key;
		}

		public NamespacedKey getKey() {
			return key;
		}

		public Recipe toBukkit() {
			return recipe;
		}
	}

	/*
	 * blast furnace, smoker, campfire and furnace recipes have extra parameters
	 */
	public class BurningRecipeInformation {
		private final MMORecipeChoice choice;
		private final float exp;
		private final int burnTime;

		protected BurningRecipeInformation(ConfigurationSection config) {
			choice = new MMORecipeChoice(config.getString("item"));
			exp = (float) config.getDouble("exp", 0.35);
			burnTime = config.getInt("time", 200);
		}

		public int getBurnTime() {
			return burnTime;
		}

		public MMORecipeChoice getChoice() {
			return choice;
		}

		public float getExp() {
			return exp;
		}
	}
}
