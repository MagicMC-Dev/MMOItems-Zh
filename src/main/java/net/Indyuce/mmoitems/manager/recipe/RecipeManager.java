package net.Indyuce.mmoitems.manager.recipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Recipe;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.api.Type;
import net.Indyuce.mmoitems.api.recipe.MMORecipeChoice;
import net.Indyuce.mmoitems.api.recipe.workbench.CustomRecipe;

public abstract class RecipeManager {
	private final Set<CustomRecipe> craftingRecipes = new HashSet<>();
	private final Set<LoadedRecipe> loadedRecipes = new HashSet<>();

	public abstract void reload();

	public abstract void registerFurnaceRecipe(Type type, String id, BurningRecipeInformation info, String number);
	public abstract void registerShapedRecipe(Type type, String id, List<String> list);
	public abstract void registerShapelessRecipe(Type type, String id, List<String> ingredients);

	public void registerRecipe(NamespacedKey key, Recipe recipe) {
		loadedRecipes.add(new LoadedRecipe(key, recipe));
	}

	public void registerRecipe(CustomRecipe recipe) {
		if(!recipe.isEmpty())
			craftingRecipes.add(recipe);
	}
	
	public Set<LoadedRecipe> getLoadedRecipes() {
		return loadedRecipes;
	}
	
	public Set<CustomRecipe> getCustomRecipes() {
		return craftingRecipes;
	}

	public Set<NamespacedKey> getNamespacedKeys() {
		return loadedRecipes.stream().map(recipe -> recipe.getKey()).collect(Collectors.toSet());
	}

	public void sortRecipes() {
		List<CustomRecipe> temporary = new ArrayList<>();
		temporary.addAll(craftingRecipes);
		craftingRecipes.clear();
		craftingRecipes.addAll(temporary.stream().sorted().collect(Collectors.toList()));
	}
	
	public void clearCustomRecipes() {
		craftingRecipes.clear();
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

		public Recipe getRecipe() {
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
